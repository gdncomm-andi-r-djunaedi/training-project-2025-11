import { api } from './client.js';

/**
 * Checkout API service
 * Communicates with the Checkout microservice via API Gateway
 */
export const checkoutApi = {
  /**
   * Validate cart and reserve inventory for checkout
   * Creates a checkout session with reserved items
   */
  async validate() {
    return api.post('/checkout/validate', {}, { requiresAuth: true });
  },

  /**
   * Get checkout by ID
   * @param {string} checkoutId - Checkout ID
   */
  async getById(checkoutId) {
    return api.get('/checkout', { params: { checkout_id: checkoutId }, requiresAuth: true });
  },

  /**
   * Finalize checkout (after payment)
   * @param {string} checkoutId - Checkout ID
   * @param {string} orderId - Order ID from payment
   */
  async finalize(checkoutId, orderId) {
    return api.post('/checkout/finalize', { 
      checkout_id: checkoutId, 
      order_id: orderId 
    }, { requiresAuth: true });
  },

  /**
   * Cancel/invalidate checkout and release reservations
   * @param {string} checkoutId - Checkout ID to cancel
   */
  async cancel(checkoutId) {
    return api.post('/checkout/cancel', { 
      checkout_id: checkoutId 
    }, { requiresAuth: true });
  }
};
