package com.zhzd.henu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class HenuApplication {

    public static void main(String[] args) {
        SpringApplication.run(HenuApplication.class, args);
    }

}
