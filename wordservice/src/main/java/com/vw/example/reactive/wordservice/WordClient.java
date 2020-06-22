package com.vw.example.reactive.wordservice;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@RequiredArgsConstructor
public class WordClient {

//    private final WordHandler wordHandler;
//
//    @Bean
//    public Flux<String> findWordsbySearch(){
//        return route(GET("/words/search").and(accept(APPLICATION_JSON)), wordHandler::findWords);
//    }
}
