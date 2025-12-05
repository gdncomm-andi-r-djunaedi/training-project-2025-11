<script>
  import { cartCount } from '../stores/cart.js';
  import { isAuthenticated, authStore } from '../stores/auth.js';
  import { navigate } from '../router/index.js';

  let menuOpen = $state(false);

  function logout() {
    authStore.logout();
    navigate('/');
    menuOpen = false;
  }

  function toggleMenu() {
    menuOpen = !menuOpen;
  }

  function closeMenu() {
    menuOpen = false;
  }
</script>

<header class="sticky top-0 z-50 glass border-b border-[var(--color-border-light)]">
  <div class="container">
    <div class="flex items-center justify-between h-16">
      <!-- Logo -->
      <a href="/" class="flex items-center gap-3 group" onclick={closeMenu}>
        <div class="w-10 h-10 rounded-full bg-[var(--color-primary)] flex items-center justify-center shadow-lg shadow-[var(--color-primary)]/20 transition-transform group-hover:scale-105">
          <span class="text-white font-bold text-lg">W</span>
        </div>
        <span class="text-xl font-bold text-[var(--color-text)] hidden sm:block tracking-tight">
          Waroenk
        </span>
      </a>

      <!-- Navigation (Desktop) -->
      <nav class="hidden md:flex items-center gap-1">
        <a href="/" class="chip">
          Home
        </a>
        <a href="/products" class="chip">
          Products
        </a>
        <a href="/merchants" class="chip">
          Merchants
        </a>
        <a href="/search" class="chip">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          Search
        </a>
      </nav>

      <!-- Right Actions (Desktop) -->
      <div class="hidden md:flex items-center gap-3">
        <!-- Cart -->
        <a 
          href="/cart" 
          class="relative p-2.5 rounded-full bg-[var(--color-border-light)] hover:bg-[var(--color-border)] transition-colors" 
          aria-label="Shopping Cart"
        >
          <svg class="w-5 h-5 text-[var(--color-text)]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
          </svg>
          {#if $cartCount > 0}
            <span class="absolute -top-1 -right-1 bg-[var(--color-primary)] text-white text-[10px] font-bold rounded-full w-5 h-5 flex items-center justify-center shadow-md">
              {$cartCount > 9 ? '9+' : $cartCount}
            </span>
          {/if}
        </a>

        <!-- Auth -->
        {#if $isAuthenticated}
          <div class="relative group">
            <button class="flex items-center gap-2 p-1.5 rounded-full hover:bg-[var(--color-border-light)] transition-colors">
              <div class="avatar avatar-sm bg-[var(--color-text)]">
                <svg class="w-4 h-4 text-white" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                </svg>
              </div>
            </button>
            <div class="absolute right-0 mt-2 w-48 bg-[var(--color-surface)] rounded-xl shadow-xl border border-[var(--color-border-light)] opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all p-2">
              <a href="/profile" class="block px-3 py-2.5 text-sm text-[var(--color-text)] hover:bg-[var(--color-border-light)] rounded-lg transition-colors font-medium">
                Profile
              </a>
              <a href="/orders" class="block px-3 py-2.5 text-sm text-[var(--color-text)] hover:bg-[var(--color-border-light)] rounded-lg transition-colors font-medium">
                My Orders
              </a>
              <div class="divider !my-1"></div>
              <button onclick={logout} class="w-full text-left px-3 py-2.5 text-sm text-[var(--color-error)] hover:bg-red-50 rounded-lg transition-colors font-medium">
                Log out
              </button>
            </div>
          </div>
        {:else}
          <a href="/login" class="btn btn-ghost btn-sm">Log in</a>
          <a href="/register" class="btn btn-primary btn-sm">Sign up</a>
        {/if}
      </div>

      <!-- Mobile Actions -->
      <div class="flex items-center gap-2 md:hidden">
        <a href="/search" class="p-2.5 rounded-full bg-[var(--color-border-light)]" aria-label="Search">
          <svg class="w-5 h-5 text-[var(--color-text)]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </a>
        
        <a href="/cart" class="relative p-2.5 rounded-full bg-[var(--color-border-light)]" aria-label="Shopping Cart">
          <svg class="w-5 h-5 text-[var(--color-text)]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
          </svg>
          {#if $cartCount > 0}
            <span class="absolute -top-1 -right-1 bg-[var(--color-primary)] text-white text-[10px] font-bold rounded-full w-5 h-5 flex items-center justify-center">
              {$cartCount > 9 ? '9+' : $cartCount}
            </span>
          {/if}
        </a>

        <button 
          onclick={toggleMenu} 
          class="p-2.5 rounded-full bg-[var(--color-border-light)] transition-colors"
          aria-label="Toggle Menu"
        >
          <svg class="w-5 h-5 text-[var(--color-text)]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            {#if menuOpen}
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            {:else}
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
            {/if}
          </svg>
        </button>
      </div>
    </div>
  </div>

  <!-- Mobile Menu -->
  {#if menuOpen}
    <div class="md:hidden bg-[var(--color-surface)] border-t border-[var(--color-border-light)] animate-fade-in">
      <nav class="container py-4 space-y-2">
        <a href="/" onclick={closeMenu} class="flex items-center gap-3 py-3 px-4 text-[var(--color-text)] hover:bg-[var(--color-border-light)] rounded-xl transition-colors font-medium">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
          </svg>
          Home
        </a>
        <a href="/products" onclick={closeMenu} class="flex items-center gap-3 py-3 px-4 text-[var(--color-text)] hover:bg-[var(--color-border-light)] rounded-xl transition-colors font-medium">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
          </svg>
          Products
        </a>
        <a href="/merchants" onclick={closeMenu} class="flex items-center gap-3 py-3 px-4 text-[var(--color-text)] hover:bg-[var(--color-border-light)] rounded-xl transition-colors font-medium">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
          </svg>
          Merchants
        </a>

        <div class="divider !my-3"></div>
        
        {#if $isAuthenticated}
          <a href="/profile" onclick={closeMenu} class="flex items-center gap-3 py-3 px-4 text-[var(--color-text)] hover:bg-[var(--color-border-light)] rounded-xl transition-colors font-medium">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
            Profile
          </a>
          <a href="/orders" onclick={closeMenu} class="flex items-center gap-3 py-3 px-4 text-[var(--color-text)] hover:bg-[var(--color-border-light)] rounded-xl transition-colors font-medium">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
            My Orders
          </a>
          <button 
            onclick={logout} 
            class="w-full flex items-center gap-3 py-3 px-4 text-[var(--color-error)] hover:bg-red-50 rounded-xl transition-colors font-medium"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
            </svg>
            Log out
          </button>
        {:else}
          <div class="flex gap-3 pt-2 px-2">
            <a href="/login" onclick={closeMenu} class="btn btn-outline flex-1">Log in</a>
            <a href="/register" onclick={closeMenu} class="btn btn-primary flex-1">Sign up</a>
          </div>
        {/if}
      </nav>
    </div>
  {/if}
</header>
