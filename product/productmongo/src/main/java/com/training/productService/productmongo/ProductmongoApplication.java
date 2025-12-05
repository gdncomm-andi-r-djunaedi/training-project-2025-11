package com.training.productService.productmongo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
@EnableCaching
public class ProductmongoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductmongoApplication.class, args);
	}

}
