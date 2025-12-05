package com.gdn.project.waroenk.cart.service;

import com.gdn.project.waroenk.cart.dto.checkout.FinalizeCheckoutResponseDto;
import com.gdn.project.waroenk.cart.dto.checkout.ValidateCheckoutResponseDto;
import com.gdn.project.waroenk.cart.entity.Checkout;

/**
 * Service interface for checkout operations.
 */
public interface CheckoutService {
    
    /**
     * Validate cart and reserve inventory for checkout
     */
    ValidateCheckoutResponseDto validateAndReserve(String userId);
    
    /**
     * Invalidate/cancel checkout and release reservations
     */
    boolean invalidateCheckout(String checkoutId);
    
    /**
     * Get checkout by ID
     */
    Checkout getCheckout(String checkoutId);
    
    /**
     * Get checkout by user ID
     */
    Checkout getCheckoutByUser(String userId);
    
    /**
     * Finalize checkout after successful payment
     */
    FinalizeCheckoutResponseDto finalizeCheckout(String checkoutId, String orderId);
}




