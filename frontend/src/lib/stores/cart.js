import { writable, derived, get } from 'svelte/store';
import { cartApi } from '../api/index.js';
import { authStore } from './auth.js';

const STORAGE_KEY = 'waroenk_cart';

/**
 * Load cart from localStorage
 */
function loadCart() {
  if (typeof window === 'undefined') return [];
  
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
      return JSON.parse(stored);
    }
  } catch (e) {
    console.error('Failed to load cart:', e);
  }
  
  return [];
}

/**
 * Save cart to localStorage
 */
function saveCart(items) {
  if (typeof window === 'undefined') return;
  
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
  } catch (e) {
    console.error('Failed to save cart:', e);
  }
}

/**
 * Check if user is authenticated
 */
function isLoggedIn() {
  const auth = get(authStore);
  return auth && auth.token;
}

/**
 * Convert server cart data to local format
 */
function serverToLocal(serverCart) {
  if (!serverCart || !serverCart.items) return [];
  return serverCart.items.map(item => ({
    sku: item.sku,
    title: item.title,
    price: item.price_snapshot || 0,
    image: item.image_url || '',
    quantity: item.quantity || 1
  }));
}

/**
 * Create cart store
 */
function createCartStore() {
  const { subscribe, set, update } = writable(loadCart());

  return {
    subscribe,

    /**
     * Add item to cart (syncs with server if logged in)
     */
    async addItem(item) {
      // Update local state immediately for fast UX
      update(items => {
        const existingIndex = items.findIndex(i => i.sku === item.sku);
        
        let newItems;
        if (existingIndex >= 0) {
          // Update quantity
          newItems = items.map((i, idx) => 
            idx === existingIndex 
              ? { ...i, quantity: i.quantity + (item.quantity || 1) }
              : i
          );
        } else {
          // Add new item
          newItems = [...items, { ...item, quantity: item.quantity || 1 }];
        }
        
        saveCart(newItems);
        return newItems;
      });

      // Sync with server if logged in
      if (isLoggedIn()) {
        try {
          await cartApi.addItem({
            sku: item.sku,
            quantity: item.quantity || 1,
            price_snapshot: item.price || 0,
            title: item.title || '',
            image_url: item.image || ''
          });
        } catch (e) {
          console.error('Failed to sync cart with server:', e);
          // Local cart is already updated, server sync is best-effort
        }
      }
    },

    /**
     * Update item quantity (syncs with server if logged in)
     */
    async updateQuantity(sku, quantity) {
      update(items => {
        const newItems = quantity <= 0
          ? items.filter(i => i.sku !== sku)
          : items.map(i => i.sku === sku ? { ...i, quantity } : i);
        
        saveCart(newItems);
        return newItems;
      });

      // Sync with server if logged in
      if (isLoggedIn()) {
        try {
          if (quantity <= 0) {
            await cartApi.removeItem(sku);
          } else {
            await cartApi.updateItem(sku, quantity);
          }
        } catch (e) {
          console.error('Failed to sync cart with server:', e);
        }
      }
    },

    /**
     * Remove item from cart (syncs with server if logged in)
     */
    async removeItem(sku) {
      update(items => {
        const newItems = items.filter(i => i.sku !== sku);
        saveCart(newItems);
        return newItems;
      });

      // Sync with server if logged in
      if (isLoggedIn()) {
        try {
          await cartApi.removeItem(sku);
        } catch (e) {
          console.error('Failed to sync cart with server:', e);
        }
      }
    },

    /**
     * Clear entire cart (syncs with server if logged in)
     */
    async clear() {
      saveCart([]);
      set([]);

      // Sync with server if logged in
      if (isLoggedIn()) {
        try {
          await cartApi.clear();
        } catch (e) {
          console.error('Failed to clear server cart:', e);
        }
      }
    },

    /**
     * Set entire cart (e.g., after syncing with server)
     */
    setItems(items) {
      saveCart(items);
      set(items);
    },

    /**
     * Sync local cart with server on login
     * Merges local items with server cart
     */
    async syncOnLogin() {
      if (!isLoggedIn()) return;

      try {
        const localItems = loadCart();
        
        if (localItems.length > 0) {
          // Sync local items to server
          const serverCart = await cartApi.syncWithServer(localItems);
          if (serverCart) {
            const mergedItems = serverToLocal(serverCart);
            set(mergedItems);
            saveCart(mergedItems);
          }
        } else {
          // Just fetch server cart
          const serverCart = await cartApi.get();
          if (serverCart) {
            const items = serverToLocal(serverCart);
            set(items);
            saveCart(items);
          }
        }
      } catch (e) {
        console.error('Failed to sync cart on login:', e);
        // Keep local cart if sync fails
      }
    },

    /**
     * Fetch cart from server (for logged-in users)
     */
    async fetchFromServer() {
      if (!isLoggedIn()) return;

      try {
        const serverCart = await cartApi.get();
        if (serverCart) {
          const items = serverToLocal(serverCart);
          set(items);
          saveCart(items);
        }
      } catch (e) {
        console.error('Failed to fetch cart from server:', e);
      }
    },

    /**
     * Initialize store
     */
    init() {
      set(loadCart());
    }
  };
}

export const cartStore = createCartStore();

// Derived store for cart item count
export const cartCount = derived(cartStore, $cart => 
  $cart.reduce((total, item) => total + item.quantity, 0)
);

// Derived store for cart total
export const cartTotal = derived(cartStore, $cart =>
  $cart.reduce((total, item) => total + (item.price * item.quantity), 0)
);

