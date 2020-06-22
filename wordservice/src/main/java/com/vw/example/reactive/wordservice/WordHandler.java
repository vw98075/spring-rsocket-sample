package com.vw.example.reactive.wordservice;

import com.vw.example.reactive.wordservice.service.WordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
//import static org.springframework.web.reactive.function.server.EntityResponse.fromObject;

@Deprecated
@Slf4j
@RequiredArgsConstructor
public class WordHandler {

    private final WordService wordService;

    public Flux<ServerResponse> findWords(ServerRequest request) {
//        Flux<ServerResponse> notFound = Flux.ServerResponse.notFound().build();
        Flux<String> wordFlux = this.wordService.findWordsOnTrie(String.valueOf(request.attribute("text")));
        return wordFlux
                .thenMany(t -> ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(t)));
    }

}
