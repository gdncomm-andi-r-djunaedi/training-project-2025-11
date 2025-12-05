package com.ecom.gateway2.controller;

import com.ecom.gateway2.controller.GatewayApplication2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class GatewayApplication2 {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication2.class, args);
    }
}
