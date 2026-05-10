package com.makemytrip.makemytrip.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class MongoConfig {

    @Bean
    public MongoClient mongoClient() {
        // This explicitly creates the connection and completely overrides the buggy properties file
        return MongoClients.create("mongodb+srv://admin:admin@main.jyn8nhq.mongodb.net/makemytrip?retryWrites=true&w=majority");
    }
}