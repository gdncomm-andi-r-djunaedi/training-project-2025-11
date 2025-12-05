package com.blibli.cartModule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CartModuleApplication {

	public static void main(String[] args) {
		SpringApplication.run(CartModuleApplication.class, args);
	}

}
