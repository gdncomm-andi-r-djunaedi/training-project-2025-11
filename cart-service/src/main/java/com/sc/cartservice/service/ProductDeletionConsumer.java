package com.sc.cartservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProductDeletionConsumer {

    @Autowired
    private CartService cartService;

    @KafkaListener(
            topics = "${app.kafka.topic.product-deleted}",
            groupId = "cart-service"
    )
    public void consume(Map<String, String> message) {
        String productCode = message.get("productCode");

        System.out.println("Received product deletion event: " + productCode);

        cartService.removeProductFromAllCarts(productCode);
    }
}
