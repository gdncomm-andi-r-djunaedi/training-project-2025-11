package com.example.cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {
//
//    private final OrderRepository orderRepository;
//
//    private final UserRepository userRepository;
//
//    private final CartItemRepository cartItemRepository;
//
//    public OrderResponse createOrder(String userId) {
//        if(!userRepository.findById(Long.parseLong(userId)).isPresent()) {
//            System.out.println("User is not found");
//            return null;
//        }
//        if(cartItemRepository.findAllByUser_Id(Long.parseLong(userId)).isEmpty()) {
//            System.out.println("Cart is empty");
//            return null;
//        }
//
//        //Calculate total price
////        BigDecimal totalPrice;
//        List<CartItem> cartItemList = cartItemRepository.findAllByUser_Id(Long.parseLong(userId));
//        for(CartItem cartItem : cartItemList) {
//            BigDecimal totalPrice;
//        }
//
//        //Create Order
//        //Clear the cart
//        return null;
//    }
}
