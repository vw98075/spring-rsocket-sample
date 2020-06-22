package com.vw.example.reactive.wordservice.data;

import org.springframework.context.ApplicationEvent;

public class WordDataPopulationEvent extends ApplicationEvent {

    private String message;

    public WordDataPopulationEvent(Object source, String msg){
        super(source);
        this.message = msg;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "WordDataPopulisedEvent{" +
                "message='" + message + '\'' +
                '}';
    }
}
