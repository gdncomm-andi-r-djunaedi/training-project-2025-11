import { api } from './client.js';

/**
 * Cart API service
 * Communicates with the Cart microservice via API Gateway
 */
export const cartApi = {
  /**
   * Get user's cart from server
   */
  async get() {
    return api.get('/cart', { requiresAuth: true });
  },

  /**
   * Add item to cart
   * @param {Object} item - Cart item data
   * @param {string} item.sku - Product SKU
   * @param {number} item.quantity - Quantity to add
   * @param {number} item.price_snapshot - Current price
   * @param {string} item.title - Product title
   * @param {string} item.image_url - Product image URL
   */
  async addItem(item) {
    return api.post('/cart/add', item, { requiresAuth: true });
  },

  /**
   * Add multiple items to cart (bulk)
   * @param {Array} items - Array of cart items
   */
  async bulkAddItems(items) {
    return api.post('/cart/bulk-add', { items }, { requiresAuth: true });
  },

  /**
   * Update cart item quantity
   * @param {string} sku - Product SKU
   * @param {number} quantity - New quantity
   */
  async updateItem(sku, quantity) {
    return api.put('/cart/update', { sku, quantity }, { requiresAuth: true });
  },

  /**
   * Remove item from cart
   * @param {string} sku - Product SKU to remove
   */
  async removeItem(sku) {
    return api.post('/cart/remove', { sku }, { requiresAuth: true });
  },

  /**
   * Clear entire cart
   */
  async clear() {
    return api.delete('/cart/clear', { requiresAuth: true });
  },

  /**
   * Sync local cart with server cart
   * Used when user logs in to merge local items with server
   * @param {Array} localItems - Local cart items
   */
  async syncWithServer(localItems) {
    if (!localItems || localItems.length === 0) {
      // Just fetch server cart if no local items
      return this.get();
    }

    // Bulk add local items to server cart
    const items = localItems.map(item => ({
      sku: item.sku,
      quantity: item.quantity,
      price_snapshot: item.price || 0,
      title: item.title,
      image_url: item.image || ''
    }));

    return this.bulkAddItems(items);
  }
};
