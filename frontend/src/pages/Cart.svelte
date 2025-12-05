<script>
  import { cartStore, cartTotal } from '../lib/stores/cart.js';
  import { isAuthenticated } from '../lib/stores/auth.js';
  import { toastStore } from '../lib/stores/toast.js';
  import { EmptyState } from '../lib/components/index.js';
  import { navigate } from '../lib/router/index.js';

  let cart = $derived($cartStore);

  function formatPrice(price) {
    return new Intl.NumberFormat('id-ID', {
      style: 'currency',
      currency: 'IDR',
      minimumFractionDigits: 0
    }).format(price || 0);
  }

  function updateQuantity(sku, newQuantity) {
    if (newQuantity < 1) {
      removeItem(sku);
    } else {
      cartStore.updateQuantity(sku, newQuantity);
    }
  }

  function removeItem(sku) {
    cartStore.removeItem(sku);
    toastStore.success('Item removed');
  }

  function clearCart() {
    if (confirm('Clear your cart?')) {
      cartStore.clear();
      toastStore.success('Cart cleared');
    }
  }

  function proceedToCheckout() {
    if (!$isAuthenticated) {
      navigate('/login?redirect=/checkout');
    } else {
      navigate('/checkout');
    }
  }
</script>

<svelte:head>
  <title>Cart - Waroenk</title>
</svelte:head>

<div class="min-h-screen bg-[var(--color-bg)]">
  <div class="container py-6">
    <h1 class="text-lg font-semibold text-[var(--color-text)] mb-6">
      Shopping Cart
    </h1>

    {#if cart.length === 0}
      <EmptyState 
        title="Your cart is empty" 
        message="Start shopping to add items"
        icon="cart"
      />
      <div class="text-center mt-4">
        <a href="/products" class="btn btn-primary">Browse Products</a>
      </div>
    {:else}
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- Cart Items -->
        <div class="lg:col-span-2 space-y-3">
          {#each cart as item (item.sku)}
            <div class="bg-white rounded-xl border border-[var(--color-border)] p-4 flex gap-4 animate-fade-in">
              <!-- Image -->
              <div class="w-20 h-20 bg-[var(--color-bg)] rounded-lg overflow-hidden flex-shrink-0">
                {#if item.image}
                  <img src={item.image} alt={item.title} class="w-full h-full object-cover" />
                {:else}
                  <div class="w-full h-full flex items-center justify-center text-[var(--color-text-muted)]">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                  </div>
                {/if}
              </div>

              <!-- Details -->
              <div class="flex-1 min-w-0">
                <h3 class="text-sm font-medium text-[var(--color-text)] truncate">
                  {item.title}
                </h3>
                <p class="text-xs text-[var(--color-text-muted)] mb-2">SKU: {item.sku}</p>
                <p class="text-sm font-semibold text-[var(--color-text)]">
                  {formatPrice(item.price)}
                </p>

                <!-- Quantity Controls -->
                <div class="flex items-center gap-3 mt-2">
                  <div class="flex items-center border border-[var(--color-border)] rounded-lg">
                    <button 
                      onclick={() => updateQuantity(item.sku, item.quantity - 1)}
                      class="w-7 h-7 flex items-center justify-center hover:bg-[var(--color-bg)] transition-colors"
                      aria-label="Decrease quantity"
                    >
                      <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4" />
                      </svg>
                    </button>
                    <span class="w-8 text-center text-xs font-medium">{item.quantity}</span>
                    <button 
                      onclick={() => updateQuantity(item.sku, item.quantity + 1)}
                      class="w-7 h-7 flex items-center justify-center hover:bg-[var(--color-bg)] transition-colors"
                      aria-label="Increase quantity"
                    >
                      <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                      </svg>
                    </button>
                  </div>

                  <button 
                    onclick={() => removeItem(item.sku)}
                    class="text-xs text-[var(--color-error)] hover:underline"
                  >
                    Remove
                  </button>
                </div>
              </div>

              <!-- Subtotal -->
              <div class="text-right hidden sm:block">
                <p class="text-xs text-[var(--color-text-muted)]">Subtotal</p>
                <p class="text-sm font-medium text-[var(--color-text)]">
                  {formatPrice(item.price * item.quantity)}
                </p>
              </div>
            </div>
          {/each}

          <!-- Clear Cart -->
          <div class="text-right">
            <button 
              onclick={clearCart}
              class="text-xs text-[var(--color-error)] hover:underline"
            >
              Clear Cart
            </button>
          </div>
        </div>

        <!-- Order Summary -->
        <div class="lg:col-span-1">
          <div class="bg-white rounded-xl border border-[var(--color-border)] p-5 sticky top-20">
            <h2 class="font-semibold text-sm text-[var(--color-text)] mb-4">Order Summary</h2>
            
            <div class="space-y-2 mb-4 text-sm">
              <div class="flex justify-between">
                <span class="text-[var(--color-text-muted)]">Subtotal ({cart.length} items)</span>
                <span>{formatPrice($cartTotal)}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-[var(--color-text-muted)]">Shipping</span>
                <span class="text-[var(--color-success)] text-xs">Calculated at checkout</span>
              </div>
            </div>

            <div class="border-t border-[var(--color-border)] pt-3 mb-4">
              <div class="flex justify-between font-semibold">
                <span>Total</span>
                <span>{formatPrice($cartTotal)}</span>
              </div>
            </div>

            <button 
              onclick={proceedToCheckout}
              class="btn btn-primary w-full py-2.5"
            >
              {$isAuthenticated ? 'Checkout' : 'Login to Checkout'}
            </button>

            <a href="/products" class="block text-center text-xs text-[var(--color-primary)] hover:underline mt-3">
              Continue Shopping
            </a>
          </div>
        </div>
      </div>
    {/if}
  </div>
</div>
