package com.accounting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.accounting")
@EntityScan("com.accounting.entity")
@EnableJpaRepositories("com.accounting.repository")
public class AccountingPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountingPlatformApplication.class, args);
    }
} 