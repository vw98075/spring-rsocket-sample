package com.vw.example.reactive.wordservice.frontend;

import io.rsocket.SocketAcceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static java.util.UUID.randomUUID;

@Slf4j
@Component
class RSocketReceiverHandler {

    private RSocketRequester rsocketRequester;

    public RSocketReceiverHandler(RSocketRequester.Builder rsocketRequesterBuilder, RSocketStrategies strategies) {
        // generate and store a unique ID that identifies this receiver instance
        String receiver = randomUUID().toString();
        log.info("Connecting using receiver ID: {}", receiver);

        // create a new SocketAcceptor using the RSocket strategies plus a new AcceptorHandler instance
        SocketAcceptor acceptor = RSocketMessageHandler.responder(strategies, new AcceptorHandler());

        // use the RSocketRequesterBuilder to register the new SocketAcceptor
        this.rsocketRequester = rsocketRequesterBuilder
                .setupRoute("word-service")
                .setupData(receiver)
                .rsocketStrategies(strategies)
                .rsocketConnector(connector -> connector.acceptor(acceptor))
                .connectTcp("localhost", 7000)
                .block();

        //  make sure that disconnection is handled gracefully by handling the RSocket onClose() events
        this.rsocketRequester.rsocket()
                .onClose()
                .doOnError(error -> log.warn("Connection CLOSED"))
                .doFinally(consumer -> log.info("Acceptor DISCONNECTED"))
                .subscribe();
    }

    @MessageMapping("word-server-status")
    public Flux<String> statusUpdate(String status) {
        log.info("Connection {}", status);
        return Flux.interval(Duration.ofSeconds(5)).map(index -> String.valueOf(Runtime.getRuntime().freeMemory()));
    }
}

@Slf4j
class AcceptorHandler {

    @MessageMapping("client-status")
    public Flux<String> statusUpdate(String status) {
        log.info("Connection {}", status);
        return Flux.interval(Duration.ofSeconds(5)).map(index -> String.valueOf(Runtime.getRuntime().freeMemory()));
    }
}