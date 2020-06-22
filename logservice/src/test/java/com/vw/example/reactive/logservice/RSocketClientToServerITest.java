package com.vw.example.reactive.logservice;

import com.vw.example.reactive.logservice.data.Notification;
import com.vw.example.reactive.logservice.data.WordSearchLogData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

@SpringBootTest
public class RSocketClientToServerITest {

    private static RSocketRequester requester;

    @BeforeAll
    public static void setupOnce(@Autowired RSocketRequester.Builder builder, @Value("${spring.rsocket.server.port}") Integer port) {
        requester = builder
                .connectTcp("localhost", port)
                .block();
    }

    @Test
    public void testRequestGetsResponse() {
        // Send a request message (1)
        Long receivingTime = 1000000L;
        Long responseTime = 2000000L;
        Mono<Notification> result = requester
                .route("logData-back")
                .data(new WordSearchLogData("TEST", Instant.now().getEpochSecond()))
                .retrieveMono(Notification.class);

        // Verify that the response message contains the expected data (2)
        StepVerifier
                .create(result)
                .consumeNextWith(data -> {
                    assert(data.getReceivingTime() == receivingTime);
                    assert(data.getResponseTime() == responseTime);})
                .verifyComplete();
    }


//    @AfterAll
    public static void tearDownOnce() {
        requester.rsocket().dispose();
    }
}
