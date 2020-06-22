package com.vw.example.reactive.logservice.repository;

import com.vw.example.reactive.logservice.data.LogRecord;
import com.vw.example.reactive.logservice.data.WordSearchLog;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface WordSearchLogRepository extends ReactiveCrudRepository<WordSearchLog, Integer> {

    Flux<LogRecord> findBySearchTextContainsOrderBySearchTimeAsc(String character);
}
