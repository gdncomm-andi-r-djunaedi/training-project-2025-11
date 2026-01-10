package com.blibli.cartmodule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CartmoduleApplication {

	public static void main(String[] args) {
		SpringApplication.run(CartmoduleApplication.class, args);
	}

}
