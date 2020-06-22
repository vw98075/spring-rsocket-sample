# spring-rsocket-sample
A sample code to illustrate how to use RSocket interaction models in Java and Spring.

It currently contains two nodes/servers: word service and log service. And both are reactive servers built with Spring WebFlux. 

Word Service (on port 8081)

Function: it has an API for searching words with a given characters. All returned words are made by those given characters. And for each search, it is send a fire-and-forget request to the log service.

Log Service (on port 8082)

Function: it receive a word search log data and save the log data to the database.

More nodes which demostrate different RSocket interaction models will be added later. 
