package com.vw.example.reactive.wordservice.service;

import com.vw.example.reactive.wordservice.data.Word;
import com.vw.example.reactive.wordservice.data.WordDataPopulationEvent;
import com.vw.example.reactive.wordservice.repository.WordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class WordService {

    private TrieNode root;

    private Stream<String> wordStream;

    private Comparator<String> wordComparator;

    private int [] scrabbleScore;

    private final WordRepository wordRepository;

    public WordService(WordRepository wordRepository) {
        this.wordRepository = wordRepository;

        root = new TrieNode();

        wordComparator = new Comparator<String>() {
            @Override
            public int compare(String w1, String w2) {

                return getScrabbleScore(w2) - getScrabbleScore(w1);
            }
        };
        scrabbleScore = new int[TrieNode.ALPHABET_SIZE];
    }

    @PostConstruct
    private void init(){

        /**
         Points | Letters
         -------+-----------------------------
         1   | A, E, I, L, N, O, R, S, T, U
         2   | D, G
         3   | B, C, M, P
         4   | F, H, V, W, Y
         5   | K
         8   | J, X
         10   | Q, Z
         */
        Map<Integer, List<Character>> map = Stream.of(
                new AbstractMap.SimpleImmutableEntry<>(1, Arrays.asList('a', 'e', 'i', 'l', 'n', 'o', 'r', 's', 't', 'u')),
                new AbstractMap.SimpleImmutableEntry<>(2, Arrays.asList('d', 'g')),
                new AbstractMap.SimpleImmutableEntry<>(3, Arrays.asList('b', 'c', 'm', 'p')),
                new AbstractMap.SimpleImmutableEntry<>(4, Arrays.asList('f', 'h', 'v', 'w', 'y')),
                new AbstractMap.SimpleImmutableEntry<>(5, Arrays.asList('k')),
                new AbstractMap.SimpleImmutableEntry<>(8, Arrays.asList('j', 'x')),
                new AbstractMap.SimpleImmutableEntry<>(10, Arrays.asList('q', 'z')))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for(List<Character> l : map.values()){
            Integer key = 0;
            for(Integer i : map.keySet()){
                if(map.get(i).equals(l)){
                    key = i;
                    break;
                }
            }
            for(Character c : l){
                scrabbleScore[c - 'a'] = key;
            }
        }
    }

    @Async
    @EventListener
    public void datapopulationEventHandler(WordDataPopulationEvent event) {
        log.debug("Handling event {}", event.toString());

        wordRepository
                .findAll()
                .subscribe(w -> insertTextToTrie(w.getText()));
//                .buffer()
//                .blockLast()
//                .stream()
//                .forEach( w -> insertTextToTrie(w.getText()));
    }

    /**
     * Save a word.
     *
     * @param word the entity to save.
     * @return the persisted entity.
     */
    public Mono<Word> save(Word word) {
        log.debug("Request to save Word : {}", word);
        return wordRepository.save(word);
    }

    /**
     * Get all the words.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
//    @Transactional(readOnly = true)
//    public Page<Word> findAll(Pageable pageable) {
//        log.debug("Request to get all Words");
//        return wordRepository.findAll(pageable);
//    }

    /**
     * Get one word by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<Word> findOne(Long id) {
        log.debug("Request to get Word : {}", id);
        return wordRepository.findById(id);
    }

    /**
     * Delete the word by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Word : {}", id);
        wordRepository.deleteById(id);
    }
    /**
     * Delete all data
     */
    public void purge(){
        wordRepository.deleteAll();
    }

    /**
     * Inserts text into trie. If the text is prefix of trie node, just marks leaf node
     *
     * @param text the text to be inserted
     */
    public void insertTextToTrie(String text) {
//        log.debug("insert to Trie: {}", key);
        if(text == null || text.equals("") || !text.matches("^[a-zA-Z]*$")){
            log.error("Input word, {}, can't be inserted to trie because it is invalid", text);
            return;
        }

        text = text.toLowerCase();
        int length = text.length();
        TrieNode pCrawl = root;

        for (int level=0; level<length; level++){
            int index = text.charAt(level) - 'a';
            if (pCrawl.children[index] == null) {
                pCrawl.children[index] = new TrieNode();
            }
            pCrawl = pCrawl.children[index];
        }
        // make last node as leaf node
        pCrawl.leaf = true;
    }

    /**
     * Find all words in the DB. And those words only contains characters in the given text. If a character appears
     * multiple times in the text, returned words can have the character multiple times up to the number if such words
     * exists in the DB.
     *
     * @param text the text.
     * @return a list of words, which are formed with characters in the input text, from DB.
     *
     * TODO: change to Flux output type
     */
    public Flux<String> findWordsOnTrie(String text){
        log.debug("Request to find Words : {}", text);
        if(text == null || text.length() == 0)
            return Flux.fromIterable(Collections.emptyList());

        text = text.toLowerCase();
        char [] chars = text.toCharArray();
        wordStream = Stream.empty();
        findAllWords(chars, root, chars.length);

        return Flux.fromIterable(wordStream.sorted(wordComparator).collect(Collectors.toList()));
    }

    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Private methods ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

    /**
     * Implement the finding words met the condition
     *
     * @param arr array of characters
     * @param root Trie root
     * @param n a length of character array
     */
    private void findAllWords(char arr[], TrieNode root, int n) {
        log.debug("find all words : {}", Arrays.toString(arr) + ", " + n);
        // create a 'has' array that will store all present character in Arr[]
        int[] hash = new int[TrieNode.ALPHABET_SIZE];
        // initialize character appearance array
        for (int i = 0 ; i < hash.length; i++)
            hash[i] = 0;

        // Record the numbers of character appearances in the input array
        for (int i = 0 ; i < n; i++)
            hash[arr[i] - 'a']++;

        // temporary node
        TrieNode pChild = root ;

        // string to hold output words
        String str = "";

        // Traverse all matrix elements. There are only 26 character possible in character array
        for (int i = 0 ; i < TrieNode.ALPHABET_SIZE; i++)  {
            // we start searching for word in dictionary if we found a character which is child of Trie root
            if (hash[i] > 0 && pChild.children[i] != null ) {
                str = str+(char)(i + 'a');
                hash[i]--;
                searchWord(pChild.children[i], hash, str);
                hash[i]++;
                str = "";
            }
        }
    }

    /**
     * Find a word
     *
     * @param root Trie node
     * @param hash
     * @param str word to be found
     */
    private void searchWord(TrieNode root, int hash[], String str) {
        log.debug("search word : {}", str);
        // if we found word in trie / dictionary
        if (root.leaf == true) {
            wordStream = Stream.concat(Stream.of(str), wordStream);
        }

        // traverse all child's of current root
        for (int i =0; i < TrieNode.ALPHABET_SIZE; i++) {
            if (hash[i] > 0 && root.children[i] != null ) {
                // add current character
                char c = (char) (i + 'a');
                hash[i]--;
                // Recursively search reaming character of word in trie
                searchWord(root.children[i], hash, str + c);
                hash[i]++;
            }
        }
    }

    /**
     * Calculate scrabble score of a given string
     *
     * @param str an input string
     * @return scrabble score
     */
    private int getScrabbleScore(String str){

        if(str == null || str.equals("") || !str.matches("^[a-zA-Z]*$"))
            return 0;

        str = str.toLowerCase();
        char [] chars = str.toCharArray();
        int score = 0;
        for(int i=0; i<chars.length; i++){
            int idx = chars[i] - 'a';
            score += scrabbleScore[i];
        }
        return score;
    }
}

class TrieNode{

    // Alphabet size
    static final int ALPHABET_SIZE = 26;

    TrieNode[] children = new TrieNode[ALPHABET_SIZE];

    // isLeaf is true if the node represents end of a word
    boolean leaf;

    public TrieNode() {
        leaf = false;
        for (int i =0 ; i< ALPHABET_SIZE ; i++)
            children[i] = null;
    }

    @Override
    public String toString() {
        return "TrieNode{" +
                "children=" + Arrays.toString(children) +
                ", leaf=" + leaf +
                '}';
    }
}