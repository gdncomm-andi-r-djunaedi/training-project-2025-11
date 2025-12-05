// cart-service
package com.gdn.cart.service;

import com.gdn.cart.entity.Cart;
import com.gdn.cart.entity.CartItem;
import com.gdn.cart.event.ProductUpdateEvent;
import com.gdn.cart.respository.CartRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductUpdateListener {

    private final CartRepository cartRepository;

    public ProductUpdateListener(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @KafkaListener(
            topics = "product-update",
            groupId = "cart-service-group",
            containerFactory = "productUpdateKafkaListenerContainerFactory"
    )
    public void handleProductUpdate(ProductUpdateEvent event) {
        System.out.println("Received ProductUpdateEvent from Kafka: " + event);

        String productId = event.getProductId();

        List<Cart> carts = cartRepository.findByItemsProductId(productId);
        for (Cart cart : carts) {
            boolean changed = false;
            for (CartItem item : cart.getItems()) {
                if (productId.equals(item.getProductId())) {
                    item.setProductName(event.getProductName());
                    item.setPrice(event.getPrice());
                    changed = true;
                }
            }
            if (changed) {
                cart.recalculateTotal();
                cartRepository.save(cart);
            }
        }
    }
}
