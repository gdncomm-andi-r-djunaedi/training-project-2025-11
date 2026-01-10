package com.blibli.cartmodule.services.impl;

import com.blibli.cartmodule.client.ProductClient;
import com.blibli.cartmodule.dto.CartItemDto;
import com.blibli.cartmodule.dto.CartResponseDto;
import com.blibli.cartmodule.dto.ProductDto;
import com.blibli.cartmodule.dto.ViewCartResponseDto;
import com.blibli.cartmodule.entity.Cart;
import com.blibli.cartmodule.entity.CartItem;
import com.blibli.cartmodule.repository.CartRepository;
import com.blibli.cartmodule.services.CartService;
import com.blibli.cartmodule.util.TokenUtil;
import com.mongodb.DuplicateKeyException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class CartServiceImpl implements CartService {

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String ACTION_VIEW = "VIEW";
    private static final String ACTION_CREATED = "CREATED";
    private static final String ACTION_ADDED = "ADDED";
    private static final String ACTION_UPDATED = "UPDATED";
    private static final String ACTION_REMOVED = "REMOVED";
    private static final String PRODUCT_CODE_PREFIX = "PRD-";
    private static final String MESSAGE_CART_EMPTY = "Cart is empty";
    private static final String MESSAGE_CART_RETRIEVED = "Cart retrieved successfully";
    private static final String MESSAGE_PRODUCT_REMOVED = "Product removed from cart successfully";
    private static final String MESSAGE_CART_CREATED = "Cart created and product added successfully";
    private static final String MESSAGE_PRODUCT_ADDED = "New product added to cart successfully";
    private static final String MESSAGE_QUANTITY_UPDATED = "Product quantity updated in cart successfully";
    private static final String MESSAGE_CART_UPDATED = "Cart updated successfully";
    private static final String MESSAGE_PRODUCT_REMOVED_CART_DELETED = "Product removed and cart deleted successfully";
    private static final String MESSAGE_CART_CLEARED = "Cart cleared successfully";
    private static final String MESSAGE_CART_NOT_FOUND = "Cart not found or already empty";

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductClient productClient;

    @Override
    public CartResponseDto addProductToCart(String memberId, String productCode, int quantity) {
        validateInputs(memberId, productCode);
        validateProductCode(productCode);

        if (quantity == 0) {
            validateRemoveOperation(memberId, productCode);
        } else {
            validateProductExists(productCode);
        }

        CartInfo cartInfo = findOrCreateCart(memberId);
        CartUpdateResult updateResult = updateCartItem(cartInfo.getCart(), productCode, quantity);
        updateCartQuantity(cartInfo.getCart());

        return cartSave(cartInfo.getCart(), productCode, updateResult, cartInfo.isNewCart());
    }

    private void validateInputs(String memberId, String productCode) {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member ID cannot be empty"
            );
        }
        if (productCode == null || productCode.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product code cannot be empty"
            );
        }
    }

    private void validateProductCode(String productCode) {
        if (!productCode.startsWith(PRODUCT_CODE_PREFIX)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid product code: Product code must start with 'PRD-'" + productCode
            );
        }
    }

    private void validateProductExists(String productCode) {
        try {
            log.debug("Validating product existence for productCode: {}", productCode);
            ProductDto product = productClient.getProductDetails(productCode);
            if (product == null) {
                log.warn("Product service returned null for productCode: {}", productCode);
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product with code '" + productCode + "' not found in product database"
                );
            }
            log.debug("Product validated successfully: productCode={}, name={}", 
                    productCode, product.getName());
        } catch (FeignException.NotFound e) {
            log.error("Product not found in product service for productCode: {}. Status: {}, Message: {}", 
                    productCode, e.status(), e.contentUTF8());
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product with code '" + productCode + "' not found in product database"
            );
        } catch (FeignException e) {
            String errorContent = e.contentUTF8();
            log.error("Error validating product existence for productCode: {}. Status: {}, Message: {}, Content: {}", 
                    productCode, e.status(), e.getMessage(), errorContent);
            
            if (e.status() == 500 && errorContent != null) {
                String lowerContent = errorContent.toLowerCase();
                if ((lowerContent.contains("\"product\"") || lowerContent.contains("'product'") || 
                     lowerContent.contains("product") && lowerContent.contains("null")) &&
                    (lowerContent.contains("is null") || lowerContent.contains("null"))) {
                    log.warn("Product service returned 500 with null product error, treating as product not found for productCode: {}", productCode);
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product with code '" + productCode + "' not found in product database"
                    );
                }
            }
            
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to validate product. Product service error: " + e.getMessage()
            );
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error validating product existence for productCode: {}", productCode, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error validating product: " + e.getMessage()
            );
        }
    }

    private void validateRemoveOperation(String memberId, String productCode) {
        Optional<Cart> cartOptional = cartRepository.findByMemberId(memberId);

        if (!cartOptional.isPresent()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                    "Cannot remove product , Cart does not exist for this memberId " + memberId
                );
            }

        Cart cart = cartOptional.get();
        if (isCartEmpty(cart)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cannot remove product: Cart is empty"
                );
            }

        if (!isProductInCart(cart, productCode)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                    "Cannot remove product , Product with code '" + productCode + "' is not present in the cart"
            );
        }
    }

    private CartInfo findOrCreateCart(String memberId) {
        Optional<Cart> cartOptional = cartRepository.findByMemberId(memberId);

        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            ensureItemsInitialized(cart);
            return new CartInfo(cart, false);
        } else {
            return new CartInfo(createNewCart(memberId), true);
        }
    }

    private Cart createNewCart(String memberId) {
        Cart cart = new Cart();
            cart.setMemberId(memberId);
            cart.setItems(new ArrayList<>());
            cart.setQuantity(0);
        return cart;
    }

    private void ensureItemsInitialized(Cart cart) {
        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }
    }

    private CartUpdateResult updateCartItem(Cart cart, String productCode, int quantity) {
        Optional<CartItem> existingItem = findCartItem(cart, productCode);
        return existingItem.map(cartItem -> handleExistingItem(cartItem, cart, quantity)).orElseGet(() -> handleNewItem(cart, productCode, quantity));
    }

    private Optional<CartItem> findCartItem(Cart cart, String productCode) {
        return cart.getItems().stream()
                .filter(item -> item != null
                        && item.getProductCode() != null
                        && item.getProductCode().equals(productCode))
                .findFirst();
    }

    private CartUpdateResult handleExistingItem(CartItem item, Cart cart, int quantity) {
            if (quantity == 0) {
                cart.getItems().remove(item);
            return new CartUpdateResult(true, false, false, 0);
        } else {
            item.setQuantity(quantity);
            return new CartUpdateResult(false, false, true, item.getQuantity());
        }
    }

    private CartUpdateResult handleNewItem(Cart cart, String productCode, int quantity) {
            if (quantity == 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                    "Cannot remove product , Product with code '" + productCode + "' is not present in the cart"
                );
        }
                CartItem newItem = new CartItem();
                newItem.setProductCode(productCode);
                newItem.setQuantity(quantity);
                cart.getItems().add(newItem);
        return new CartUpdateResult(false, true, false, quantity);
        }

    private void updateCartQuantity(Cart cart) {
        int totalQuantity = cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        cart.setQuantity(totalQuantity);
    }

    private CartResponseDto cartSave(Cart cart, String productCode, CartUpdateResult updateResult, boolean isNewCart) {
        try {
            if (isCartEmpty(cart) && cart.getId() != null) {
                cartRepository.delete(cart);
                return buildRemovedResponse(productCode, true);
            }

            cartRepository.save(cart);
            return buildSuccessResponse(cart, productCode, updateResult, isNewCart);

        } catch (DuplicateKeyException e) {
            cartRepository.findByMemberId(cart.getMemberId())
                    .orElseThrow(() -> new RuntimeException("Cart not found after duplicate key error"));
            return addProductToCart(cart.getMemberId(), productCode,
                    updateResult.isItemRemoved() ? 0 : updateResult.getFinalQuantity());
        }
    }

    private CartResponseDto buildSuccessResponse(Cart cart, String productCode, CartUpdateResult updateResult, boolean isNewCart) {
        CartResponseDto response = new CartResponseDto();
        response.setProductCode(productCode);
        response.setStatus(STATUS_SUCCESS);
        response.setQuantity(updateResult.getFinalQuantity());

        if (updateResult.isItemRemoved()) {
            response.setMessage(MESSAGE_PRODUCT_REMOVED);
            response.setAction(ACTION_REMOVED);
        } else if (isNewCart && updateResult.isNewItem()) {
            response.setMessage(MESSAGE_CART_CREATED);
            response.setAction(ACTION_CREATED);
        } else if (updateResult.isNewItem()) {
            response.setMessage(MESSAGE_PRODUCT_ADDED);
            response.setAction(ACTION_ADDED);
        } else if (updateResult.isQuantityUpdated()) {
            response.setMessage(MESSAGE_QUANTITY_UPDATED);
            response.setAction(ACTION_UPDATED);
        } else {
            response.setMessage(MESSAGE_CART_UPDATED);
            response.setAction(ACTION_UPDATED);
        }

        return response;
    }

    private CartResponseDto buildRemovedResponse(String productCode, boolean cartDeleted) {
        CartResponseDto response = new CartResponseDto();
        response.setProductCode(productCode);
        response.setStatus(STATUS_SUCCESS);
        response.setMessage(cartDeleted ? MESSAGE_PRODUCT_REMOVED_CART_DELETED : MESSAGE_PRODUCT_REMOVED);
        response.setAction(ACTION_REMOVED);
        response.setQuantity(0);
        return response;
    }

    private ViewCartResponseDto createEmptyCartResponse() {
        ViewCartResponseDto response = new ViewCartResponseDto();
        response.setStatus(STATUS_SUCCESS);
        response.setAction(ACTION_VIEW);
        response.setMessage(MESSAGE_CART_EMPTY);
        response.setTotalCartQuantity(0);
        response.setTotalPrice(0.0);
        response.setItems(new ArrayList<>());
        return response;
    }

    private ViewCartResponseDto convertToViewDTO(Cart cart) {
        ViewCartResponseDto response = new ViewCartResponseDto();
        response.setStatus(STATUS_SUCCESS);
        response.setAction(ACTION_VIEW);
        response.setMessage(MESSAGE_CART_RETRIEVED);
        response.setTotalCartQuantity(cart.getQuantity());
        
        List<CartItemDto> enrichedItems = new ArrayList<>();
        Double totalPrice = 0.0;

        if (cart.getItems() != null && !cart.getItems().isEmpty()) {
            for (CartItem item : cart.getItems()) {
                CartItemDto itemDto = itemDetails(item);
                enrichedItems.add(itemDto);
                if (itemDto.getPrice() != null) {
                    totalPrice += itemDto.getPrice();
                }
            }
        }
        
        response.setItems(enrichedItems);
        response.setTotalPrice(totalPrice);  // ← ADD THIS

        return response;
    }

    private CartItemDto itemDetails(CartItem item) {
        CartItemDto itemDto = new CartItemDto();
        itemDto.setProductCode(item.getProductCode());
        itemDto.setQuantity(item.getQuantity());
        
        if (item.getProductCode() != null) {
            try {
                log.debug("Fetching product details for productCode: {}", item.getProductCode());
                ProductDto product = productClient.getProductDetails(item.getProductCode());
                if (product != null) {
                    log.debug("Product found: name={}, price={}, imageUrl={}", 
                            product.getName(), product.getPrice(), product.getImageUrl());
                    itemDto.setProductId(product.getProductCode());
                    itemDto.setProductName(product.getName());
                    Double totalPrice = product.getPrice() * item.getQuantity();
                    itemDto.setPrice(totalPrice);
                    itemDto.setImageUrl(product.getImageUrl());
                } else {
                    log.warn("Product service returned null for productCode: {}", item.getProductCode());
                }
            } catch (FeignException.NotFound e) {
                log.warn("Product not found for productCode: {}. Status: {}, Message: {}", 
                        item.getProductCode(), e.status(), e.contentUTF8());
            } catch (FeignException e) {
                log.error("Error fetching product details for productCode: {}. Status: {}, Message: {}, Content: {}", 
                        item.getProductCode(), e.status(), e.getMessage(), e.contentUTF8());
            } catch (Exception e) {
                log.error("Unexpected error fetching product details for productCode: {}", 
                        item.getProductCode(), e);
            }
        } else {
            log.warn("ProductCode is null, skipping product details fetch");
        }
        
        return itemDto;
    }

    private boolean isCartEmpty(Cart cart) {
        return cart.getItems() == null
                || cart.getItems().isEmpty()
                || cart.getQuantity() == 0;
    }

    private boolean isProductInCart(Cart cart, String productCode) {
        return cart.getItems().stream()
                .anyMatch(item -> item != null
                        && item.getProductCode() != null
                        && item.getProductCode().equals(productCode));
    }

    private static class CartInfo {
        private final Cart cart;
        private final boolean newCart;

        public CartInfo(Cart cart, boolean newCart) {
            this.cart = cart;
            this.newCart = newCart;
        }

        public Cart getCart() { return cart; }
        public boolean isNewCart() { return newCart; }
    }

    private static class CartUpdateResult {
        private final boolean itemRemoved;
        private final boolean newItem;
        private final boolean quantityUpdated;
        private final int finalQuantity;

        public CartUpdateResult(boolean itemRemoved, boolean newItem, boolean quantityUpdated, int finalQuantity) {
            this.itemRemoved = itemRemoved;
            this.newItem = newItem;
            this.quantityUpdated = quantityUpdated;
            this.finalQuantity = finalQuantity;
        }

        public boolean isItemRemoved() { return itemRemoved; }
        public boolean isNewItem() { return newItem; }
        public boolean isQuantityUpdated() { return quantityUpdated; }
        public int getFinalQuantity() { return finalQuantity; }
    }

    @Override
    public String clearCart(String memberId) {
        Optional<Cart> cartOpt = cartRepository.findByMemberId(memberId);

        if (cartOpt.isPresent()) {
            cartRepository.delete(cartOpt.get());
            return MESSAGE_CART_CLEARED;
        } else {
            return MESSAGE_CART_NOT_FOUND;
        }
    }

    @Override
    public ViewCartResponseDto viewCart(String token) {
        log.info("Viewing cart - extracting memberId from token");
        
        String memberId = TokenUtil.extractMemberIdFromToken(token);
        log.debug("Extracted memberId from token: {}", memberId);
        
        validateMemberId(memberId);
        
        Optional<Cart> cartOpt = cartRepository.findByMemberId(memberId);

        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            if (isCartEmpty(cart)) {
                log.debug("Cart is empty for memberId: {}", memberId);
                return createEmptyCartResponse();
            }
            log.debug("Cart retrieved for memberId: {}, totalItems: {}, totalQuantity: {}", 
                    memberId, cart.getItems().size(), cart.getQuantity());
            return convertToViewDTO(cart);
        } else {
            log.debug("Cart not found for memberId: {}", memberId);
            return createEmptyCartResponse();
        }
    }

    @Override
    public void validateMemberId(String memberId) {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member ID cannot be empty"
            );
        }
        
        log.debug("MemberId validation passed: {}", memberId);
    }
}
