<script>
  import { onMount, onDestroy } from 'svelte';
  import { searchApi, categoriesApi } from '../lib/api/index.js';
  import { ProductCard, Loading, EmptyState } from '../lib/components/index.js';

  let searchParams = new URLSearchParams(window.location.search);
  
  let query = $state(searchParams.get('q') || '');
  let categoryId = $state(searchParams.get('category') || '');
  let products = $state([]);
  let categories = $state([]);
  let loading = $state(true);
  let loadingMore = $state(false);
  let error = $state(null);
  let totalMatch = $state(0);
  let totalDocument = $state(0);
  let elapsedTime = $state(0);
  let nextToken = $state(null);
  const pageSize = 12;
  
  // Lazy load
  let loadMoreRef = $state(null);
  let observer = null;

  async function loadProducts(cursor = null) {
    if (cursor) {
      loadingMore = true;
    } else {
      loading = true;
    }
    error = null;
    
    try {
      const startTime = performance.now();
      
      // Build search params with category filter
      const searchParams = {
        query: query,
        size: pageSize,
        cursor: cursor
      };
      
      // Add category filter if selected
      if (categoryId) {
        searchParams.category_id = categoryId;
      }
      
      const res = await searchApi.products(searchParams);
      const endTime = performance.now();
      
      if (!cursor) {
        elapsedTime = Math.round(endTime - startTime);
      }
      
      if (cursor) {
        products = [...products, ...(res.contents || [])];
      } else {
        products = res.contents || [];
      }
      
      totalMatch = res.total_match || 0;
      totalDocument = res.total_document || totalMatch;
      nextToken = res.next_token || null;
    } catch (e) {
      error = e.message;
    } finally {
      loading = false;
      loadingMore = false;
    }
  }

  async function loadCategories() {
    try {
      const res = await categoriesApi.filter({ size: 20 });
      categories = res.data || [];
    } catch (e) {
      console.error('Failed to load categories:', e);
    }
  }

  function setupIntersectionObserver() {
    if (loadMoreRef) {
      observer = new IntersectionObserver(
        (entries) => {
          if (entries[0].isIntersecting && nextToken && !loadingMore && !loading) {
            loadProducts(nextToken);
          }
        },
        { threshold: 0.1, rootMargin: '100px' }
      );
      observer.observe(loadMoreRef);
    }
  }

  function cleanupObserver() {
    if (observer) {
      observer.disconnect();
      observer = null;
    }
  }

  onMount(() => {
    loadCategories();
    loadProducts();
  });
  
  onDestroy(() => {
    cleanupObserver();
  });
  
  // Re-setup observer when ref changes
  $effect(() => {
    cleanupObserver();
    if (loadMoreRef) {
      setupIntersectionObserver();
    }
  });
  
  // React to category changes
  $effect(() => {
    // Only trigger if categoryId changes (after initial mount)
    const currentCat = categoryId;
    if (typeof currentCat === 'string') {
      // Update URL with category
      const url = new URL(window.location.href);
      if (currentCat) {
        url.searchParams.set('category', currentCat);
      } else {
        url.searchParams.delete('category');
      }
      if (query) {
        url.searchParams.set('q', query);
      } else {
        url.searchParams.delete('q');
      }
      window.history.replaceState({}, '', url.toString());
    }
  });

  function handleSearch(e) {
    e.preventDefault();
    loadProducts();
  }
  
  function handleCategoryChange() {
    // Reset and reload products when category changes
    loadProducts();
  }
</script>

<svelte:head>
  <title>Products - Waroenk</title>
</svelte:head>

<div class="min-h-screen">
  <!-- Page Header -->
  <div class="bg-white border-b border-[var(--color-border)]">
    <div class="container py-6">
      <h1 class="text-lg font-semibold text-[var(--color-text)] mb-4">
        {query ? `Results for "${query}"` : 'All Products'}
      </h1>

      <!-- Filters -->
      <div class="flex flex-col sm:flex-row gap-3">
        <!-- Search -->
        <form onsubmit={handleSearch} class="flex-1">
          <div class="relative">
            <input
              type="text"
              bind:value={query}
              placeholder="Search products..."
              class="input pl-9 bg-[var(--color-bg)]"
            />
            <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[var(--color-text-muted)]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
        </form>

        <!-- Category Filter -->
        <select
          bind:value={categoryId}
          onchange={handleCategoryChange}
          class="input w-full sm:w-48 bg-[var(--color-bg)]"
        >
          <option value="">All Categories</option>
          {#each categories as cat}
            <option value={cat.id}>{cat.name}</option>
          {/each}
        </select>
      </div>

      <!-- Results Count with Search Info -->
      {#if !loading && totalMatch > 0}
        <p class="text-sm text-[var(--color-text-muted)] mt-3">
          Found <span class="font-semibold text-[var(--color-text)]">{totalMatch}</span> products 
          out of <span class="font-semibold text-[var(--color-text)]">{totalDocument}</span> documents 
          in <span class="font-semibold text-[var(--color-primary)]">{elapsedTime} ms</span>
        </p>
      {/if}
    </div>
  </div>

  <!-- Products Grid -->
  <div class="container py-6">
    {#if loading && products.length === 0}
      <Loading text="Loading products..." />
    {:else if error}
      <EmptyState title="Failed to load products" message={error} icon="error" />
    {:else if products.length === 0}
      <EmptyState 
        title="No products found" 
        message="Try adjusting your search"
        icon="product"
      />
    {:else}
      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
        {#each products as product, i}
          <ProductCard {product} index={i} />
        {/each}
      </div>

      <!-- Lazy load sentinel -->
      {#if nextToken}
        <div bind:this={loadMoreRef} class="text-center mt-8 py-4">
          {#if loadingMore}
            <div class="flex items-center justify-center gap-2 text-[var(--color-text-muted)]">
              <svg class="w-5 h-5 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
              <span>Loading more...</span>
            </div>
          {:else}
            <span class="text-[var(--color-text-muted)] text-sm">Scroll for more</span>
          {/if}
        </div>
      {/if}
    {/if}
  </div>
</div>
