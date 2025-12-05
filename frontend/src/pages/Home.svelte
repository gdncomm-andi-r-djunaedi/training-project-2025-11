<script>
  import { onMount, onDestroy } from 'svelte';
  import { searchApi, categoriesApi, brandsApi } from '../lib/api/index.js';
  import { ProductCard, MerchantCard, CategoryCard, Loading, EmptyState } from '../lib/components/index.js';
  import { navigate } from '../lib/router/index.js';
  import { isAuthenticated } from '../lib/stores/auth.js';

  let categories = $state([]);
  let brands = $state([]);
  let featuredProducts = $state([]);
  let featuredMerchants = $state([]);
  let totalProducts = $state(0);
  let totalMerchants = $state(0);
  let loading = $state(true);
  let error = $state(null);
  
  // Cursor tracking for gradient
  let mouseX = $state(50);
  let mouseY = $state(50);
  let heroRef = $state(null);
  
  function handleMouseMove(e) {
    if (!heroRef) return;
    const rect = heroRef.getBoundingClientRect();
    mouseX = ((e.clientX - rect.left) / rect.width) * 100;
    mouseY = ((e.clientY - rect.top) / rect.height) * 100;
  }

  onMount(async () => {
    try {
      const [categoriesRes, brandsRes, searchRes] = await Promise.all([
        categoriesApi.filter({ size: 8 }).catch(() => ({ data: [] })),
        brandsApi.filter({ size: 8 }).catch(() => ({ data: [] })),
        searchApi.combined({ query: '', size: 8 }).catch(() => ({ products: { contents: [], total_document: 0 }, merchants: { contents: [], total_document: 0 } }))
      ]);

      categories = categoriesRes.data || [];
      brands = brandsRes.data || [];
      featuredProducts = searchRes.products?.contents || [];
      featuredMerchants = searchRes.merchants?.contents || [];
      totalProducts = searchRes.products?.total_document || searchRes.products?.total_match || featuredProducts.length;
      totalMerchants = searchRes.merchants?.total_document || searchRes.merchants?.total_match || featuredMerchants.length;
    } catch (e) {
      error = e.message;
    } finally {
      loading = false;
    }
  });

  function handleCategoryClick(category) {
    navigate(`/search?q=${encodeURIComponent(category.name)}`);
  }

  function handleBrandClick(brand) {
    navigate(`/search?q=${encodeURIComponent(brand.name)}`);
  }

  function formatCount(num) {
    if (num >= 1000) {
      return (num / 1000).toFixed(num >= 10000 ? 0 : 1) + 'K+';
    }
    return num.toString() + '+';
  }
</script>

<svelte:head>
  <title>Waroenk - Your Trusted Marketplace</title>
</svelte:head>

<!-- Hero Section - Enhanced with cursor-following gradient -->
<section 
  bind:this={heroRef}
  onmousemove={handleMouseMove}
  class="relative overflow-hidden hero-gradient-bg"
>
  <!-- Cursor-following gradient background with 3 colors -->
  <div 
    class="absolute inset-0 hero-gradient-cursor transition-all duration-300 ease-out"
    style="--mouse-x: {mouseX}%; --mouse-y: {mouseY}%;"
  ></div>
  <div class="absolute inset-0 hero-gradient-overlay"></div>
  
  <div class="container py-20 md:py-32 lg:py-40 relative z-10">
    <div class="max-w-3xl">
      <div class="animate-fade-in" style="opacity: 0;">
        <span class="badge badge-success mb-6">
          ✨ Your marketplace
        </span>
      </div>
      
      <h1 class="text-4xl md:text-6xl lg:text-7xl font-bold text-[var(--color-text)] mb-8 tracking-tight animate-fade-in stagger-1" style="opacity: 0;">
        Find products<br/>
        you'll <span class="text-[var(--color-primary)]">love</span>
      </h1>
      
      <p class="text-lg md:text-xl text-[var(--color-text-light)] mb-12 max-w-xl leading-relaxed animate-fade-in stagger-2" style="opacity: 0;">
        Discover thousands of quality products from trusted local merchants. Shop smarter, support local.
      </p>

      <!-- Action Buttons -->
      <div class="flex flex-wrap gap-4 animate-fade-in stagger-3" style="opacity: 0;">
        <a href="/products" class="btn btn-primary btn-lg">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
          </svg>
          Browse Products
        </a>
        <a href="/search" class="btn btn-outline btn-lg">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          Search
        </a>
      </div>

      <!-- Stats with real data -->
      <div class="flex flex-wrap gap-10 mt-14 animate-fade-in stagger-4" style="opacity: 0;">
        <div class="stat-item">
          <p class="text-3xl md:text-4xl font-bold text-[var(--color-text)] stat-number">{loading ? '...' : formatCount(totalProducts)}</p>
          <p class="text-sm text-[var(--color-text-muted)] mt-1">Products</p>
        </div>
        <div class="stat-item">
          <p class="text-3xl md:text-4xl font-bold text-[var(--color-text)] stat-number">{loading ? '...' : formatCount(totalMerchants)}</p>
          <p class="text-sm text-[var(--color-text-muted)] mt-1">Merchants</p>
        </div>
        <div class="stat-item">
          <p class="text-3xl md:text-4xl font-bold text-[var(--color-primary)]">Fast</p>
          <p class="text-sm text-[var(--color-text-muted)] mt-1">Delivery</p>
        </div>
      </div>
    </div>
  </div>
  
  <!-- Decorative floating orbs - 3 colors: green, light purple, light blue -->
  <div class="absolute top-1/4 right-10 w-72 h-72 bg-[#1db954]/20 rounded-full blur-3xl animate-float pointer-events-none"></div>
  <div class="absolute bottom-1/4 right-1/4 w-48 h-48 bg-[#a78bfa]/15 rounded-full blur-2xl animate-float-delayed pointer-events-none"></div>
  <div class="absolute top-1/3 left-1/4 w-56 h-56 bg-[#67e8f9]/15 rounded-full blur-3xl animate-float pointer-events-none" style="animation-delay: -2s;"></div>
</section>

<style>
  .hero-gradient-bg {
    background: var(--color-bg);
    position: relative;
  }
  
  .hero-gradient-cursor {
    background: 
      radial-gradient(
        circle at var(--mouse-x, 50%) var(--mouse-y, 50%),
        rgba(29, 185, 84, 0.25) 0%,
        transparent 35%
      ),
      radial-gradient(
        circle at calc(var(--mouse-x, 50%) + 20%) calc(var(--mouse-y, 50%) - 15%),
        rgba(167, 139, 250, 0.2) 0%,
        transparent 30%
      ),
      radial-gradient(
        circle at calc(var(--mouse-x, 50%) - 15%) calc(var(--mouse-y, 50%) + 20%),
        rgba(103, 232, 249, 0.2) 0%,
        transparent 30%
      );
  }
  
  .hero-gradient-overlay {
    background: 
      linear-gradient(
        135deg,
        rgba(29, 185, 84, 0.05) 0%,
        transparent 40%,
        rgba(167, 139, 250, 0.03) 60%,
        rgba(103, 232, 249, 0.05) 100%
      );
    animation: pulseGlow 8s ease-in-out infinite;
  }
  
  @keyframes pulseGlow {
    0%, 100% { opacity: 0.7; }
    50% { opacity: 1; }
  }
  
  @keyframes float {
    0%, 100% { transform: translateY(0) rotate(0deg); }
    50% { transform: translateY(-20px) rotate(5deg); }
  }
  
  @keyframes floatDelayed {
    0%, 100% { transform: translateY(0) rotate(0deg); }
    50% { transform: translateY(-15px) rotate(-5deg); }
  }
  
  .animate-float {
    animation: float 8s ease-in-out infinite;
  }
  
  .animate-float-delayed {
    animation: floatDelayed 10s ease-in-out infinite;
    animation-delay: -3s;
  }
  
  .stat-item {
    position: relative;
  }
  
  .stat-number {
    background: linear-gradient(135deg, var(--color-text) 0%, var(--color-text-light) 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
</style>

{#if loading}
  <section class="section">
    <div class="container">
      <!-- Skeleton for Categories -->
      <div class="mb-12">
        <div class="skeleton skeleton-text w-32 h-6 mb-6"></div>
        <div class="grid grid-cols-4 sm:grid-cols-8 gap-3">
          {#each Array(8) as _}
            <div class="skeleton h-20 rounded-xl"></div>
          {/each}
        </div>
      </div>
      
      <!-- Skeleton for Products -->
      <div>
        <div class="skeleton skeleton-text w-40 h-6 mb-6"></div>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          {#each Array(8) as _}
            <div class="skeleton h-64 rounded-xl"></div>
          {/each}
        </div>
      </div>
    </div>
  </section>
{:else if error}
  <section class="section">
    <div class="container">
      <EmptyState title="Something went wrong" message={error} icon="error" />
    </div>
  </section>
{:else}
  <!-- Categories Section -->
  {#if categories.length > 0}
    <section class="section bg-[var(--color-surface)]">
      <div class="container">
        <div class="flex items-center justify-between mb-8">
          <h2 class="section-title">
            Browse by Category
          </h2>
          <a href="/categories" class="text-sm font-semibold text-[var(--color-text)] hover:text-[var(--color-primary)] transition-colors">
            See all →
          </a>
        </div>
        <div class="grid grid-cols-4 sm:grid-cols-8 gap-3">
          {#each categories as category, i}
            <button 
              onclick={() => handleCategoryClick(category)}
              class="group flex flex-col items-center p-4 rounded-2xl bg-[var(--color-surface)] hover:bg-[var(--color-primary)]/5 border border-transparent hover:border-[var(--color-primary)]/20 transition-all duration-300 animate-fade-in stagger-{(i % 8) + 1}"
              style="opacity: 0;"
            >
              <!-- Icon -->
              <div class="w-12 h-12 rounded-2xl overflow-hidden bg-[var(--color-border-light)] mb-3 transition-transform duration-300 group-hover:scale-110">
                {#if category.icon_url}
                  <img 
                    src={category.icon_url} 
                    alt={category.name}
                    class="w-full h-full object-cover"
                    loading="lazy"
                  />
                {:else}
                  <div class="w-full h-full flex items-center justify-center bg-gradient-to-br from-[var(--color-primary)]/10 to-[var(--color-primary)]/5">
                    <svg class="w-6 h-6 text-[var(--color-primary)]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                    </svg>
                  </div>
                {/if}
              </div>
              <!-- Name -->
              <h3 class="text-xs font-semibold text-[var(--color-text)] text-center group-hover:text-[var(--color-primary)] transition-colors truncate w-full">
                {category.name}
              </h3>
            </button>
          {/each}
        </div>
      </div>
    </section>
  {/if}

  <!-- Brands Section -->
  {#if brands.length > 0}
    <section class="section">
      <div class="container">
        <div class="flex items-center justify-between mb-8">
          <div>
            <span class="section-subtitle">Shop by</span>
            <h2 class="section-title mt-1">
              Popular Brands
            </h2>
          </div>
          <a href="/brands" class="btn btn-outline btn-sm hidden sm:flex">
            View all
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
          </a>
        </div>
        <div class="grid grid-cols-4 sm:grid-cols-8 gap-3">
          {#each brands as brand, i}
            <button 
              onclick={() => handleBrandClick(brand)}
              class="group flex flex-col items-center p-4 rounded-2xl bg-[var(--color-surface)] hover:bg-[var(--color-primary)]/5 border border-transparent hover:border-[var(--color-primary)]/20 transition-all duration-300 animate-fade-in stagger-{(i % 8) + 1}"
              style="opacity: 0;"
            >
              <!-- Logo -->
              <div class="w-12 h-12 rounded-2xl overflow-hidden bg-[var(--color-border-light)] mb-3 transition-transform duration-300 group-hover:scale-110">
                {#if brand.logo_url}
                  <img 
                    src={brand.logo_url} 
                    alt={brand.name}
                    class="w-full h-full object-contain p-1"
                    loading="lazy"
                  />
                {:else}
                  <div class="w-full h-full flex items-center justify-center bg-gradient-to-br from-[var(--color-primary)]/10 to-[var(--color-primary)]/5">
                    <span class="text-lg font-bold text-[var(--color-primary)]">
                      {brand.name?.charAt(0)?.toUpperCase() || 'B'}
                    </span>
                  </div>
                {/if}
              </div>
              <!-- Name -->
              <h3 class="text-xs font-semibold text-[var(--color-text)] text-center group-hover:text-[var(--color-primary)] transition-colors truncate w-full">
                {brand.name}
              </h3>
            </button>
          {/each}
        </div>
        <div class="mt-8 text-center sm:hidden">
          <a href="/brands" class="btn btn-outline">
            View all brands
          </a>
        </div>
      </div>
    </section>
  {/if}

  <!-- Featured Products Section -->
  {#if featuredProducts.length > 0}
    <section class="section bg-[var(--color-surface)]">
      <div class="container">
        <div class="flex items-center justify-between mb-8">
          <div>
            <span class="section-subtitle">Popular right now</span>
            <h2 class="section-title mt-1">
              Featured Products
            </h2>
          </div>
          <a href="/products" class="btn btn-outline btn-sm hidden sm:flex">
            View all
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
          </a>
        </div>
        <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 md:gap-6">
          {#each featuredProducts as product, i}
            <ProductCard {product} index={i} />
          {/each}
        </div>
        <div class="mt-8 text-center sm:hidden">
          <a href="/products" class="btn btn-outline">
            View all products
          </a>
        </div>
      </div>
    </section>
  {/if}

  <!-- Featured Merchants Section -->
  {#if featuredMerchants.length > 0}
    <section class="section">
      <div class="container">
        <div class="flex items-center justify-between mb-8">
          <div>
            <span class="section-subtitle">Trusted sellers</span>
            <h2 class="section-title mt-1">
              Top Merchants
            </h2>
          </div>
          <a href="/merchants" class="btn btn-outline btn-sm hidden sm:flex">
            View all
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
          </a>
        </div>
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6">
          {#each featuredMerchants as merchant, i}
            <MerchantCard {merchant} index={i} />
          {/each}
        </div>
        <div class="mt-8 text-center sm:hidden">
          <a href="/merchants" class="btn btn-outline">
            View all merchants
          </a>
        </div>
      </div>
    </section>
  {/if}

  <!-- CTA Section - Clean & Minimal (only show when not authenticated) -->
  {#if !$isAuthenticated}
    <section class="section-lg bg-gradient-to-br from-[var(--color-text)] to-[#282828]">
      <div class="container text-center">
        <div class="max-w-2xl mx-auto">
          <h2 class="text-2xl md:text-4xl font-bold text-white mb-4 tracking-tight">
            Ready to start shopping?
          </h2>
          <p class="text-[#a7a7a7] mb-8 text-lg">
            Create an account to track your orders, save favorites, and get personalized recommendations.
          </p>
          <div class="flex flex-wrap justify-center gap-4">
            <a href="/register" class="btn btn-primary btn-lg">
              Create free account
            </a>
            <a href="/login" class="btn btn-lg" style="background: rgba(255,255,255,0.1); color: white; border: 1px solid rgba(255,255,255,0.2);">
              Log in
            </a>
          </div>
        </div>
      </div>
    </section>
  {/if}
{/if}
