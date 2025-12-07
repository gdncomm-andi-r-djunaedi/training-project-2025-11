package com.marketplace.seeder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.marketplace.seeder",
        "com.marketplace.common"
})
@EntityScan(basePackages = "com.marketplace.member.entity")
@EnableJpaRepositories(basePackages = "com.marketplace.member.repository")
@EnableMongoRepositories(basePackages = "com.marketplace.product.repository")
public class DataSeederApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataSeederApplication.class, args);
    }
}

