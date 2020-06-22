package com.vw.example.reactive.wordservice.repository;

import com.vw.example.reactive.wordservice.data.Word;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 * Spring Data  repository for the Word entity.
 */
//@SuppressWarnings("unused")
//@Repository
public interface WordRepository extends ReactiveCrudRepository<Word, Long> {

}
