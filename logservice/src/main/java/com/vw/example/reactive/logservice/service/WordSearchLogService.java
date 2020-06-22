package com.vw.example.reactive.logservice.service;

import com.vw.example.reactive.logservice.data.LogRecord;
import com.vw.example.reactive.logservice.data.SearchStatisticData;
import com.vw.example.reactive.logservice.repository.WordSearchLogRepository;
import com.vw.example.reactive.logservice.data.WordSearchLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@RequiredArgsConstructor
@Service
public class WordSearchLogService {

    private final WordSearchLogRepository repository;

    public Mono<WordSearchLog> processLogData(WordSearchLog log){

        return repository.save(log);
    }

    public Mono<Long> dataCount(){
        return repository.count();
    }

    public Flux<WordSearchLog> getAll(){
        return repository.findAll();
    }

    public Flux<SearchStatisticData> findStatisticData(Character c, long period){

        return this.repository.findBySearchTextContainsOrderBySearchTimeAsc(String.valueOf(c))
                //Concat with impossible value
                .concatWithValues(new LogRecord() {
                    @Override
                    public Long getSearchTime() {
                        return -1L;
                    }
                })
                //Create accumulator, process value and return
                .scan(new UpbondAccumulator(c, period), UpbondAccumulator::process)
                //Get results, note if there are no results, this will be empty meaning it isn't passed on in chain
                .flatMap(UpbondAccumulator::getResult);
    }

    class UpbondAccumulator{

        final Long period;
        Character c;
        Long upbond;
        Integer count;
        Long point;
        Boolean first;
        Queue<SearchStatisticData> results;

        UpbondAccumulator(Character character, Long period){
            this.c = character;
            this.period = period;
            this.count = 0;
            this.upbond = 0L;
            this.results = new ConcurrentLinkedQueue<>();
            this.first = true;
        }

        //Logic is inside accumulator, since accumulator is the only the only thing
        //that needs it. Allows reuse of accumulator w/o code repetition
        public UpbondAccumulator process(LogRecord inRecord){
            Long in = inRecord.getSearchTime();
            //If impossible value, Add current count to queue and return. You will have to determine what is impossible
            if(in<0L){
                results.add(new SearchStatisticData(this.c, count, point));
                return this;
            }
            //If first value, do stuff outside loop
            if(this.first) {
                upbond = in + period;
                point = in;
                first=false;
            }

            if(in <= upbond)
                count++;
            else {
                results.add(new SearchStatisticData(this.c, count, point));
                count = 1;
                point = in;
                upbond += period;
            }
            //Return accumulator. This could be put elsewhere since it isn't Immediately obvious
            // that `process` should return the object but is simpler for example
            return this;
        }

        public Mono<SearchStatisticData> getResult() {
            //Return mono empty if queue is empty, Otherwise return queued result
            return Mono.justOrEmpty(results.poll());
        }
    }
}
