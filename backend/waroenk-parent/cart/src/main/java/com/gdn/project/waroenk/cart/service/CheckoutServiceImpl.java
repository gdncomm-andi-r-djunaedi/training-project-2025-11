package com.gdn.project.waroenk.cart.service;

import com.gdn.project.waroenk.cart.dto.checkout.CheckoutItemDto;
import com.gdn.project.waroenk.cart.dto.checkout.CheckoutResponseDto;
import com.gdn.project.waroenk.cart.dto.checkout.FinalizeCheckoutResponseDto;
import com.gdn.project.waroenk.cart.dto.checkout.ValidateCheckoutResponseDto;
import com.gdn.project.waroenk.cart.entity.Cart;
import com.gdn.project.waroenk.cart.entity.Checkout;
import com.gdn.project.waroenk.cart.entity.CheckoutItem;
import com.gdn.project.waroenk.cart.exceptions.ResourceNotFoundException;
import com.gdn.project.waroenk.cart.exceptions.ValidationException;
import com.gdn.project.waroenk.cart.mapper.CheckoutMapper;
import com.gdn.project.waroenk.cart.repository.CheckoutRepository;
import com.gdn.project.waroenk.cart.utility.CacheUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CheckoutServiceImpl implements CheckoutService {

    private static final CheckoutMapper mapper = CheckoutMapper.INSTANCE;
    private static final String CHECKOUT_PREFIX = "checkout";

    private final CheckoutRepository repository;
    private final CartService cartService;
    private final SystemParameterService systemParameterService;
    private final CacheUtil<String> cacheUtil;
    private final ObjectMapper objectMapper;

    @Value("${cart.checkout.ttl-seconds:900}")
    private Integer defaultCheckoutTtl;

    @Value("${cart.checkout.use-redis:true}")
    private Boolean useRedis;

    public CheckoutServiceImpl(CheckoutRepository repository,
                               CartService cartService,
                               SystemParameterService systemParameterService,
                               CacheUtil<String> cacheUtil,
                               ObjectMapper objectMapper) {
        this.repository = repository;
        this.cartService = cartService;
        this.systemParameterService = systemParameterService;
        this.cacheUtil = cacheUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public ValidateCheckoutResponseDto validateAndReserve(String userId) {
        // Get cart
        Cart cart = cartService.getCart(userId);
        
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return new ValidateCheckoutResponseDto(null, false, "Cart is empty", new ArrayList<>());
        }

        // Check for existing active checkout
        Checkout existingCheckout = repository.findByUserIdAndStatus(userId, "RESERVED").orElse(null);
        if (existingCheckout != null && !existingCheckout.isExpired()) {
            CheckoutResponseDto checkoutDto = mapper.toResponseDto(existingCheckout);
            return new ValidateCheckoutResponseDto(checkoutDto, true, "Existing checkout found", new ArrayList<>());
        }

        // Get TTL from system parameters
        int ttl = getCheckoutTtl();

        // Create checkout items from cart
        List<CheckoutItem> checkoutItems = cart.getItems().stream()
                .map(cartItem -> CheckoutItem.builder()
                        .sku(cartItem.getSku())
                        .quantity(cartItem.getQuantity())
                        .priceSnapshot(cartItem.getPriceSnapshot())
                        .title(cartItem.getTitle())
                        .reserved(true) // TODO: Implement actual inventory reservation
                        .build())
                .collect(Collectors.toList());

        // Validate and reserve inventory (simplified - TODO: integrate with inventory service)
        List<ValidateCheckoutResponseDto.ValidationErrorDto> errors = validateInventory(checkoutItems);
        
        if (!errors.isEmpty()) {
            return new ValidateCheckoutResponseDto(null, false, "Some items failed validation", errors);
        }

        // Create checkout
        String checkoutId = "chk-" + UUID.randomUUID().toString().substring(0, 8);
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(ttl);

        Checkout checkout = Checkout.builder()
                .checkoutId(checkoutId)
                .userId(userId)
                .sourceCartId(cart.getId())
                .items(checkoutItems)
                .totalAmount(cart.getTotalAmount())
                .status("RESERVED")
                .lockedAt(now)
                .expiresAt(expiresAt)
                .build();

        // Save to MongoDB
        Checkout saved = repository.save(checkout);

        // Save to Redis if enabled
        if (Boolean.TRUE.equals(useRedis)) {
            saveToRedis(saved, ttl);
        }

        CheckoutResponseDto checkoutDto = mapper.toResponseDto(saved);
        return new ValidateCheckoutResponseDto(checkoutDto, true, "Checkout created successfully", new ArrayList<>());
    }

    @Override
    public boolean invalidateCheckout(String checkoutId) {
        Checkout checkout = getCheckout(checkoutId);
        
        // Release inventory reservations (TODO: integrate with inventory service)
        releaseReservations(checkout);

        // Update status
        checkout.setStatus("CANCELLED");
        repository.save(checkout);

        // Remove from Redis
        if (Boolean.TRUE.equals(useRedis)) {
            removeFromRedis(checkoutId);
        }

        return true;
    }

    @Override
    public Checkout getCheckout(String checkoutId) {
        // Try Redis first
        if (Boolean.TRUE.equals(useRedis)) {
            Checkout cached = getFromRedis(checkoutId);
            if (cached != null) {
                return cached;
            }
        }

        // Fallback to MongoDB
        return repository.findByCheckoutId(checkoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkout with ID " + checkoutId + " not found"));
    }

    @Override
    public Checkout getCheckoutByUser(String userId) {
        return repository.findByUserIdAndStatus(userId, "RESERVED")
                .orElseThrow(() -> new ResourceNotFoundException("No active checkout found for user " + userId));
    }

    @Override
    public FinalizeCheckoutResponseDto finalizeCheckout(String checkoutId, String orderId) {
        Checkout checkout = getCheckout(checkoutId);

        if (!"RESERVED".equals(checkout.getStatus())) {
            throw new ValidationException("Checkout is not in RESERVED status");
        }

        if (checkout.isExpired()) {
            checkout.setStatus("EXPIRED");
            repository.save(checkout);
            throw new ValidationException("Checkout has expired");
        }

        // Finalize inventory (TODO: integrate with inventory service)
        // This would typically confirm the inventory deduction

        // Update checkout status
        checkout.setStatus("FINALIZED");
        repository.save(checkout);

        // Remove from Redis
        if (Boolean.TRUE.equals(useRedis)) {
            removeFromRedis(checkoutId);
        }

        // Clear the cart
        cartService.clearCart(checkout.getUserId());

        return new FinalizeCheckoutResponseDto(true, "Checkout finalized successfully", orderId);
    }

    private int getCheckoutTtl() {
        try {
            Integer ttl = systemParameterService.getInt("cart.checkout.ttl-seconds", defaultCheckoutTtl);
            return ttl != null ? ttl : defaultCheckoutTtl;
        } catch (Exception e) {
            log.warn("Failed to get checkout TTL from system parameters, using default: {}", defaultCheckoutTtl);
            return defaultCheckoutTtl;
        }
    }

    private List<ValidateCheckoutResponseDto.ValidationErrorDto> validateInventory(List<CheckoutItem> items) {
        // TODO: Implement actual inventory validation by calling inventory service
        // For now, return empty list (all valid)
        return new ArrayList<>();
    }

    private void releaseReservations(Checkout checkout) {
        // TODO: Implement actual inventory release by calling inventory service
        log.info("Releasing reservations for checkout: {}", checkout.getCheckoutId());
    }

    private void saveToRedis(Checkout checkout, int ttl) {
        try {
            String key = CHECKOUT_PREFIX + ":" + checkout.getCheckoutId();
            String value = objectMapper.writeValueAsString(checkout);
            cacheUtil.putValue(key, value, ttl, TimeUnit.SECONDS);
            log.debug("Saved checkout to Redis: {}", key);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize checkout to Redis: {}", e.getMessage());
        }
    }

    private Checkout getFromRedis(String checkoutId) {
        try {
            String key = CHECKOUT_PREFIX + ":" + checkoutId;
            String value = cacheUtil.getValue(key);
            if (ObjectUtils.isNotEmpty(value)) {
                return objectMapper.readValue(value, Checkout.class);
            }
        } catch (Exception e) {
            log.warn("Failed to deserialize checkout from Redis: {}", e.getMessage());
        }
        return null;
    }

    private void removeFromRedis(String checkoutId) {
        String key = CHECKOUT_PREFIX + ":" + checkoutId;
        cacheUtil.removeValue(key);
    }
}




