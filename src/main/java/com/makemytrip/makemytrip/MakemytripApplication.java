package com.makemytrip.makemytrip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MakemytripApplication {

    public static void main(String[] args) {
        System.setProperty("spring.data.mongodb.uri", "mongodb+srv://admin:admin@main.jyn8nhq.mongodb.net/makemytrip?retryWrites=true&w=majority");
        SpringApplication.run(MakemytripApplication.class, args);
    }

}
