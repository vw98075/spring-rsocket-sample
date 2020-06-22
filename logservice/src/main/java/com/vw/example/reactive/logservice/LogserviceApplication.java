package com.vw.example.reactive.logservice;

import com.vw.example.reactive.logservice.repository.WordSearchLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.BaseStream;

@Slf4j
@SpringBootApplication
public class LogserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogserviceApplication.class, args);
    }

    @Bean
    public ApplicationRunner seeder(DatabaseClient client, WordSearchLogRepository repository) {
        return args -> getSchema()
                .flatMap(sql -> executeSql(client, sql))
                .doOnSuccess(count -> log.info("Schema created"))
                .then(repository.deleteAll())
                .subscribe(v -> log.info("Repository cleared"));
    }

    private Mono<Integer> executeSql(DatabaseClient client, String sql) {
        return client.execute(sql).fetch().rowsUpdated();
    }

    private Mono<String> getSchema() throws URISyntaxException {
        Path path = Paths.get(ClassLoader.getSystemResource("schema.sql").toURI());
        return Flux
                .using(() -> Files.lines(path), Flux::fromStream, BaseStream::close)
                .reduce((line1, line2) -> line1 + "\n" + line2);
    }
}

@Slf4j
@Component
@RequiredArgsConstructor
class WordDataInitializer {

    private final WordSearchLogRepository repository;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeDB() {

        log.info("Count in DB: " + repository.count().block().toString());
//
//		RSocketServer.create()
//				// Enable Zero Copy
//				.payloadDecoder(PayloadDecoder.ZERO_COPY)
//				.bind(TcpServerTransport.create(7000))
//				.block()
//				.onClose()
//				.block();
    }
}