package com.notabene;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.notabene", "com.example.myspringapp"})
@EnableJpaRepositories(basePackages = {"com.notabene", "com.example.myspringapp"})
@EntityScan(basePackages = {"com.notabene", "com.example.myspringapp"})
public class NotaBeneApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotaBeneApplication.class, args);
    }
}
