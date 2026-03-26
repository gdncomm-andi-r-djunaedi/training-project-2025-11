package com.blibli.cartData;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.blibli.cartData.client")
public class CartDataApplication {

	public static void main(String[] args) {
		SpringApplication.run(CartDataApplication.class, args);
	}

}
