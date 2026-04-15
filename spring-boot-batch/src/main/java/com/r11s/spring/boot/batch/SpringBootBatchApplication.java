package com.r11s.spring.boot.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootBatchApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(SpringBootBatchApplication.class, args)));
    }

}
