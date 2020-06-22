package com.vw.example.reactive.wordservice.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordSearchLogData {

    private String searchText;
    private Long searchTime;

    public WordSearchLogData(String searchText, Instant searchTime) {
       this(searchText, searchTime.getEpochSecond());
    }
}
