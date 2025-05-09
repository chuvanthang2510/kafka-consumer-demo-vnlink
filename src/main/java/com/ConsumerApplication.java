package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.couchbase.CouchbaseAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.couchbase.CouchbaseDataAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.couchbase.CouchbaseRepositoriesAutoConfiguration.class
})
public class ConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}
