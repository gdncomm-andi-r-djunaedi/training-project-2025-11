package com.example.cart.eventlistener;

import com.example.cart.dto.ProductUpdateEvent;
import com.example.cart.entity.CartEntity;
import com.example.cart.repository.CartRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductUpdateEventListener {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CartRepository cartRepository;

    @KafkaListener(topics = {"product.update.event"})
    public void productUpdateConsumed(String message) throws Exception {
        ProductUpdateEvent productUpdateEvent = objectMapper.readValue(message, ProductUpdateEvent.class);
        List<CartEntity> cartEntities = cartRepository.findAllByProductId(productUpdateEvent.getProductId());
        for(CartEntity cartEntity:cartEntities){
            cartEntity.setProductName(productUpdateEvent.getProductName());
            cartEntity.setItemPrice(productUpdateEvent.getItemPrice());
            cartEntity.setTotalPrice(cartEntity.getItemPrice()*cartEntity.getProductQuantity());
        }
        cartRepository.saveAll(cartEntities);
    }
}
