package com.example.cart.eventlistener;

import com.example.cart.dto.ProductDeleteEvent;
import com.example.cart.repository.CartRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductDeleteEventListener {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    CartRepository cartRepository;

    @Transactional
    @KafkaListener(topics = {"product.delete.event"})
    public void productDeleteConsumed(String message)throws Exception{
        ProductDeleteEvent productDeleteEvent = objectMapper.readValue(message,ProductDeleteEvent.class);
        cartRepository.deleteAllByProductId(productDeleteEvent.getProductId());
    }
}
