package com.vw.example.reactive.wordservice.frontend;

import com.vw.example.reactive.wordservice.data.Notification;
import com.vw.example.reactive.wordservice.data.WordSearchLogData;
import com.vw.example.reactive.wordservice.service.WordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@RestController
public class WordController {

    private final WordService wordService;

    private final Mono<RSocketRequester> rsocketRequester;

    public WordController(WordService wordService, Mono<RSocketRequester> rsocketRequester) {
        this.wordService = wordService;
        this.rsocketRequester = rsocketRequester;
    }

    @GetMapping("/words/search")
    public Flux<String> findWordsContainCharacters(@RequestParam final String text){

        rsocketRequester
            .flatMap(requester -> requester.route("log-data")
            .data(new WordSearchLogData(text, Instant.now()))
            .send())
//            .retrieveMono(Notification.class))
            .subscribe(n -> log.info("Received notification: {}", n));

        return wordService.findWordsOnTrie(text).map(s -> new String(s + " "));
    }
}

//@Slf4j
//class ClientHandler {
//
//    @MessageMapping("client-status")
//    public Flux<String> statusUpdate(String status) {
//        log.info("Connection {}", status);
//        return Flux.interval(Duration.ofSeconds(5)).map(index -> String.valueOf(Runtime.getRuntime().freeMemory()));
//    }
//}