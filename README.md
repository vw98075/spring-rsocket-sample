## Spring Rsocket Sample

A sample code to illustrate how to use RSocket interaction models in Java and Spring.

Currently, it contains two nodes/servers: word service and log service. And both are reactive servers built with Spring WebFlux.

### Word Search Service

The core Word Search Service API is for searching words with a given character. All returned words are formed by the given characters. It is running on localhost port 8081. 

And for each search, it sends log data as a fire-and-forget request to the log service. An alternative RScoket implementation, request-and-response also is provided.

### Log Service

Ths log service hosts the word search log data. It is running on localhost port 8082. It receives a word search log data from the word service and saves the log data to the database.

This log service also sends a request to the word search service for its memory usage. The word search service will respond to the request with free memory data every five seconds. 

More nodes which demonstrate different RSocket interaction models will be added later.