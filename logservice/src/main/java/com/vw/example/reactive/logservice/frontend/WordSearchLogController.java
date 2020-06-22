package com.vw.example.reactive.logservice.frontend;

import com.vw.example.reactive.logservice.data.SearchStatisticData;
import com.vw.example.reactive.logservice.data.WordSearchLog;
import com.vw.example.reactive.logservice.service.WordSearchLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

}
