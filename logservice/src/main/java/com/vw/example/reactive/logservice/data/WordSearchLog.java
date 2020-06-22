package com.vw.example.reactive.logservice.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WordSearchLog {

    @Id
    private Long id;

    private String searchText;

    private Long searchTime;

    public WordSearchLog(WordSearchLogData data) {
        this.searchText = data.getSearchText();
        this.searchTime = data.getSearchTime();
    }
}
