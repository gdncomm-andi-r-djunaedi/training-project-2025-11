package project_cart.cart.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project_cart.cart.entity.Cart;
import project_cart.cart.entity.CartItem;
import project_cart.cart.model.CartRequest;
import project_cart.cart.model.CartItemRequest;
import project_cart.cart.repository.CartItemRepository;
import project_cart.cart.repository.CartRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    // =========== Cart SERVICES
    //
    // Get All Cart
    public List<Cart> getCarts() {
        return cartRepository.findAll();
    }

    // Get Cart By Id
    public Optional<Cart> getCartById(String id) {
        return cartRepository.findByUserId(id);
    }

    // Create Cart & Update Cart
    public Cart saveCart(Cart cart) {
        return cartRepository.save(cart);
    }

    // Add To Cart
    public Cart addToCart(CartRequest cartRequest) {
        Cart dbCart = getCartById(cartRequest.getUserId())
                .orElseGet(
                        () -> Cart.builder()
                                .userId(cartRequest.getUserId())
                                .build()
                );

        if (dbCart.getId() == null) {
            CartItem cartItem = CartItem.builder()
                    .cart(dbCart)
                    .productId(cartRequest.getProductId())
                    .quantity(cartRequest.getQuantity())
                    .build();
            saveCart(dbCart);
            cartItemRepository.save(cartItem);
            return dbCart;
        }

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(dbCart.getId(), cartRequest.getProductId())
                .orElseGet(() -> CartItem.builder()
                        .cart(dbCart)
                        .productId(cartRequest.getProductId())
                        .quantity(0)
                        .build());

        cartItem.setQuantity(cartItem.getQuantity() + cartRequest.getQuantity());
        saveCart(dbCart);
        cartItemRepository.save(cartItem);

        return cartRepository.save(dbCart);
    }

    // =========== Cart Item SERVICES
    //
    //  Get Cart Item
    public List<CartItem> getCartItemById(Long id) {
        return cartItemRepository.findByCartId(id);
    }

    // Delete Cart Item
    public void deleteCartItem(CartItemRequest cartItemRequest) {
        Cart dbCart = getCartById(cartItemRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("Cart Not Found"));

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(dbCart.getId(), cartItemRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("CartItem Not Found"));

        cartItemRepository.delete(cartItem);
    }

    // Update Cart Item
    public void updateCartItem(CartRequest cartRequest) {
        Cart dbCart = getCartById(cartRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("Cart Not Found"));

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(dbCart.getId(), cartRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("CartItem Not Found"));

        if (cartRequest.getQuantity() < 1) {
            deleteCartItem(CartItemRequest
                    .builder()
                    .userId(cartRequest.getUserId())
                    .productId(cartRequest.getProductId())
                    .build());
            return;
        }
        cartItem.setQuantity(cartRequest.getQuantity());
        cartItemRepository.save(cartItem);
    }
}
