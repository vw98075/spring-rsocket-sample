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
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RSocketController {

    private final WordSearchLogService service;

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

    @Data
    @AllArgsConstructor
    private class LogStatisticDataRequest {
        Character character;
        Long period;
    }
}
