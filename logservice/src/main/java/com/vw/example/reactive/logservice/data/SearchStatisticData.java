package com.vw.example.reactive.logservice.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchStatisticData {

    Character c;
    int numberOfCounts;
    long searchTime;
}
