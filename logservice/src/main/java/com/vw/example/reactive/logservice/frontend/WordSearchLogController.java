package com.vw.example.reactive.logservice.frontend;

import com.vw.example.reactive.logservice.data.SearchStatisticData;
import com.vw.example.reactive.logservice.data.WordSearchLog;
import com.vw.example.reactive.logservice.service.WordSearchLogService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/word-search-log")
@RequiredArgsConstructor
public class WordSearchLogController {

    private final WordSearchLogService service;

    @GetMapping("/total")
    public Mono<Long> getTotal(){
        return service.dataCount();
    }

    @GetMapping
    public Flux<WordSearchLog> getAllLogData(){
        return service.getAll();
    }

    @GetMapping("/statistics")
    public Flux<SearchStatisticData> getStatisticData(@RequestParam String c, @RequestParam(defaultValue = "20000" ) String p){
        return service.findStatisticData(c.charAt(0), Long.valueOf(p));
    }

//    @GetMapping("/word-service-status")    TODO: refine this method
    public String getWordServiceStatus(RSocketRequester requester, @Payload String receiver){

        requester.route("word-server-status")
                .data("OPEN")
                .retrieveFlux(String.class)
                .doOnNext(s -> //new WordServiceStatus(receiver, s)).
                log.info("Receiver: {} Free Memory: {}.",receiver,s))
                .subscribe();

        return "Success";
    }

    @Data
    private class WordServiceStatus {

        private String receiver;
        private String freeMemory;

        public WordServiceStatus(String receiver, String s) {
            this.receiver = receiver;
            this.freeMemory = s;
        }
    }
}
