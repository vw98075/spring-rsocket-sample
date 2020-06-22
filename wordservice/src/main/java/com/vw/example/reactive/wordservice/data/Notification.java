package com.vw.example.reactive.wordservice.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    Long receivingTime;
    Long responseTime;
}
