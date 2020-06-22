package com.vw.example.reactive.wordservice;

import com.vw.example.reactive.wordservice.data.Word;
import com.vw.example.reactive.wordservice.data.WordDataPopulationEvent;
import com.vw.example.reactive.wordservice.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Slf4j
@SpringBootApplication
public class WordserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WordserviceApplication.class, args);
	}

	@Bean
	public ApplicationRunner seeder(DatabaseClient client, WordRepository repository) {
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
		Path path = Paths.get(getClass()
				.getClassLoader()
				.getResource("schema.sql")
				.toURI()
		);//ClassLoader.getSystemResource("schema.sql").toURI());
		return Flux
				.using(() -> Files.lines(path), Flux::fromStream, BaseStream::close)
				.reduce((line1, line2) -> line1 + "\n" + line2);
	}

	private Mono<RSocketRequester> requesterMono;

	@Bean
	public Mono<RSocketRequester> myRequester(RSocketRequester.Builder rsocketRequesterBuilder, RSocketStrategies strategies) {

		requesterMono = rsocketRequesterBuilder
				.rsocketStrategies(strategies)
				.connectTcp("localhost", 7000);

		return requesterMono;
	}

	@PreDestroy
	void shutdown() {
		log.info("shut down ...");
		requesterMono.block().rsocket().dispose();
	}

//	@Bean
//	RouterFunction<ServerResponse> findWordsWithCharacters(WordService wordService) {
//       return route().GET("/words/search/{text}", req -> ok().body(wordService.findWordsOnTrie(req.pathVariable("text"))))
//                .build();

//		return RouterFunctions
//				.route(GET("/words/search/{text}").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), req -> wordService.findWordsOnTrie(req.pathVariable("text")));
//	}

}

@Slf4j
@Component
@RequiredArgsConstructor
class WordDataInitializer {

	private final WordRepository wordRepository;
	private final ApplicationEventPublisher applicationEventPublisher;

	@EventListener(ApplicationReadyEvent.class)
	public void initializeDB() throws URISyntaxException, IOException {

		Path path = Paths
				.get(getClass()
						.getClassLoader()
						.getResource("words.txt")
						.toURI());

		Stream<String> ws = Files
				.lines(path)
				.filter(value -> value != null && value.length() > 0 && value.indexOf("\'") == -1)
				.distinct();

		Publisher<?> data =
				Flux.fromStream(ws)
						.map(word -> new Word(word.trim()))
						.flatMap(wordRepository::save);

		wordRepository
				.deleteAll()
				.thenMany(data)
				.thenMany(wordRepository.findAll())
				.subscribe(s -> log.info(String.valueOf(s)));

		log.info("-------------------------------");
		Long total = wordRepository.count().block();
		log.info("The total number of words: {}", total);
		applicationEventPublisher.publishEvent(new WordDataPopulationEvent(this, "Word data populish is completed with a total of " + total + " data entries"));
	}

}