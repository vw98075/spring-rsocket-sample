package com.vw.example.reactive.logservice.frontend;

import com.vw.example.reactive.logservice.data.Notification;
import com.vw.example.reactive.logservice.data.SearchStatisticData;
import com.vw.example.reactive.logservice.data.WordSearchLog;
import com.vw.example.reactive.logservice.data.WordSearchLogData;
import com.vw.example.reactive.logservice.service.WordSearchLogService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RSocketController {

    private final WordSearchLogService service;
    private final List<RSocketRequester> RECEIVERS = new ArrayList<>();

    // fire-n-forget
    @MessageMapping("log-data")
    public Mono<Void> collectMarketData(WordSearchLogData wordLog) {
        log.info("Received fire-and-forget request: " + wordLog.toString());
        service.processLogData(new WordSearchLog(wordLog)).subscribe(c -> log.info(c.toString()));
        return Mono.empty();
    }

    @MessageMapping("log-data-back")
    public Notification get(WordSearchLogData wordLog) {
        log.info("Received request-response request: {}", wordLog);
        service.processLogData(new WordSearchLog(wordLog)).subscribe(c -> log.info(c.toString()));
        service.dataCount().subscribe( c -> log.info("After processing: {}", c));
        Notification notification = new Notification(wordLog.getSearchTime(), Instant.now().getEpochSecond());
        return notification;
    }

    @MessageMapping("log-statistic-data")
    public Flux<SearchStatisticData> getLogStatisticData(LogStatisticDataRequest request){
        log.info("Received stream request: {}", request);
        return service.findStatisticData(request.character, request.period);
    }

    @ConnectMapping("word-service")
    void connectShellClientAndAskForTelemetry(RSocketRequester requester, @Payload String receiver) {
        log.info("connectShellClientAndAskForTelemetry requester: {} and payload {}", requester, receiver);
        requester.rsocket()
                .onClose() // This method returns a reactive Mono object, which contains all the callbacks you need
                .doFirst(() -> {
                    log.info("Receiver: {} CONNECTED.", receiver);
                    RECEIVERS.add(requester); // the mono’s doFirst() method gets called before any calls to subscribe(),
                    // but after the initial creation of the mono. Use this method to add the receiver’s requester object
                    // to the CLIENTS list
                })
                .doOnError(error -> {
                    log.warn("Channel to receiver {} CLOSED", receiver); // RSocket calls the mono’s doOnError() method
                    // whenever there is a problem with the connection. This includes situations where the receiver
                    // has chosen to close the connection. You can use the error variable provided to decide
                    // what action to take. In the code above, the error simply gets logged as a warning
                })
                .doFinally(consumer -> {
                    RECEIVERS.remove(requester);
                    log.info("Receiver {} DISCONNECTED", receiver); // The mono’s doFinally() method is triggered
                    // when the RSocket connection has closed. This method is the ideal place to run the code
                    // that removes the receiver from the list CLIENTS
                })
                .subscribe(); // subscribe() activates the reactive code you’ve added to the mono and signals
        // that you’re ready to process the events

        //        Callback to receiver, confirming connection
        requester.route("client-status")
                .data("OPEN")
                .retrieveFlux(String.class)
                .doOnNext(s -> log.info("Receiver: {} Free Memory: {}.",receiver,s))
                .subscribe();
    }

    @Data
    @AllArgsConstructor
    private class LogStatisticDataRequest {
        Character character;
        Long period;
    }
}
