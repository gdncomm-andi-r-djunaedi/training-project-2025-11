import { writable, derived } from 'svelte/store';

const STORAGE_KEY = 'waroenk_auth';

/**
 * Load auth state from localStorage
 */
function loadAuthState() {
  if (typeof window === 'undefined') return { user: null, token: null };
  
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
      return JSON.parse(stored);
    }
  } catch (e) {
    console.error('Failed to load auth state:', e);
  }
  
  return { user: null, token: null };
}

/**
 * Save auth state to localStorage
 */
function saveAuthState(state) {
  if (typeof window === 'undefined') return;
  
  try {
    if (state.token) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  } catch (e) {
    console.error('Failed to save auth state:', e);
  }
}

/**
 * Create auth store
 */
function createAuthStore() {
  const initialState = loadAuthState();
  const { subscribe, set, update } = writable(initialState);

  return {
    subscribe,
    
    /**
     * Set user and token after login
     */
    login(user, token) {
      const state = { user, token };
      saveAuthState(state);
      set(state);
    },

    /**
     * Clear auth state on logout
     */
    logout() {
      const state = { user: null, token: null };
      saveAuthState(state);
      set(state);
    },

    /**
     * Update user profile
     */
    updateUser(userData) {
      update(state => {
        const newState = { ...state, user: userData };
        saveAuthState(newState);
        return newState;
      });
    },

    /**
     * Initialize store (call on app mount)
     */
    init() {
      const state = loadAuthState();
      set(state);
    }
  };
}

export const authStore = createAuthStore();

// Derived store for checking if user is logged in
export const isAuthenticated = derived(authStore, $auth => !!$auth.token);

// Derived store for getting user info
export const currentUser = derived(authStore, $auth => $auth.user);

