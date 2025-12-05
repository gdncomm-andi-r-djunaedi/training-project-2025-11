<script>
  import { onMount } from 'svelte';
  import { productsApi, searchApi } from '../lib/api/index.js';
  import { cartStore } from '../lib/stores/cart.js';
  import { toastStore } from '../lib/stores/toast.js';
  import { Loading, EmptyState, ProductCard } from '../lib/components/index.js';

  let { id } = $props();
  
  let product = $state(null);
  let selectedVariant = $state(null);
  let relatedProducts = $state([]);
  let loading = $state(true);
  let error = $state(null);
  let quantity = $state(1);
  let selectedImage = $state(0);

  function formatPrice(price) {
    return new Intl.NumberFormat('id-ID', {
      style: 'currency',
      currency: 'IDR',
      minimumFractionDigits: 0
    }).format(price || 0);
  }

  onMount(async () => {
    try {
      // Try to get product details - first try the details endpoint, fallback to search
      try {
        product = await productsApi.getDetails(id);
      } catch (detailsError) {
        // Fallback: search for the product by sku/subSku
        const searchRes = await searchApi.products({
          query: id,
          size: 1
        });
        if (searchRes.contents && searchRes.contents.length > 0) {
          const searchProduct = searchRes.contents[0];
          // Map search result to product format
          product = {
            sku: searchProduct.sku,
            title: searchProduct.title,
            shortDescription: searchProduct.short_description,
            category: searchProduct.category,
            brand: searchProduct.brand,
            merchant: searchProduct.merchant,
            tags: searchProduct.tags,
            hasStock: searchProduct.has_stock,
            totalStock: searchProduct.total_stock,
            variants: [{
              subSku: searchProduct.sub_sku || searchProduct.sku,
              title: searchProduct.variant_title || 'Default',
              price: searchProduct.price,
              thumbnail: searchProduct.thumbnail,
              media: searchProduct.media || [],
              isDefault: true,
              stockInfo: {
                hasStock: searchProduct.has_stock,
                totalStock: searchProduct.total_stock
              }
            }]
          };
        } else {
          throw new Error('Product not found');
        }
      }
      
      // Select the matching variant or default variant
      if (product?.variants?.length > 0) {
        selectedVariant = product.variants.find(v => v.subSku === id) 
          || product.variants.find(v => v.isDefault) 
          || product.variants[0];
      }

      // Load related products based on category
      const categoryName = product?.category?.name;
      if (categoryName) {
        const res = await searchApi.products({
          query: categoryName,
          size: 4
        }).catch(() => ({ contents: [] }));
        relatedProducts = (res.contents || []).filter(p => 
          p.sku !== product.sku && p.sub_sku !== selectedVariant?.subSku
        );
      }
    } catch (e) {
      error = e.message;
    } finally {
      loading = false;
    }
  });

  function selectVariant(variant) {
    selectedVariant = variant;
    selectedImage = 0;
  }

  function addToCart() {
    if (!product || !selectedVariant) return;
    
    cartStore.addItem({
      sku: selectedVariant.subSku,
      title: `${product.title} - ${selectedVariant.title}`,
      price: selectedVariant.price || 0,
      image: selectedVariant.thumbnail || '/placeholder.jpg',
      quantity: quantity
    });
    
    toastStore.success(`Added ${quantity} item(s) to cart`);
  }

  // Computed values
  $effect(() => {
    // Reset image selection when variant changes
    if (selectedVariant) {
      selectedImage = 0;
    }
  });
</script>

<svelte:head>
  <title>{product?.title || 'Product'} - Waroenk</title>
</svelte:head>

<div class="min-h-screen bg-[var(--color-bg)]">
  {#if loading}
    <Loading text="Loading product..." />
  {:else if error || !product}
    <div class="container py-12">
      <EmptyState 
        title="Product not found" 
        message={error || "This product doesn't exist"} 
        icon="product" 
      />
      <div class="text-center mt-4">
        <a href="/products" class="btn btn-primary">Browse Products</a>
      </div>
    </div>
  {:else}
    <!-- Breadcrumb -->
    <div class="bg-white border-b border-[var(--color-border)]">
      <div class="container py-3">
        <nav class="flex items-center gap-2 text-xs text-[var(--color-text-muted)]">
          <a href="/" class="hover:text-[var(--color-primary)]">Home</a>
          <span>/</span>
          <a href="/products" class="hover:text-[var(--color-primary)]">Products</a>
          {#if product.category}
            <span>/</span>
            <a href="/products?category={product.category.id}" class="hover:text-[var(--color-primary)]">{product.category.name}</a>
          {/if}
          <span>/</span>
          <span class="text-[var(--color-text)] truncate">{product.title}</span>
        </nav>
      </div>
    </div>

    <!-- Product Detail -->
    <div class="container py-6">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <!-- Images -->
        <div class="space-y-3">
          <div class="aspect-square bg-white rounded-xl border border-[var(--color-border)] overflow-hidden">
            {#if selectedVariant?.media?.[selectedImage]?.url || selectedVariant?.thumbnail}
              <img 
                src={selectedVariant.media?.[selectedImage]?.url || selectedVariant.thumbnail} 
                alt={product.title}
                class="w-full h-full object-cover"
              />
            {:else}
              <div class="w-full h-full flex items-center justify-center text-[var(--color-text-muted)]">
                <svg class="w-16 h-16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
            {/if}
          </div>

          {#if selectedVariant?.media && selectedVariant.media.length > 1}
            <div class="flex gap-2 overflow-x-auto">
              {#each selectedVariant.media as media, i}
                <button
                  onclick={() => selectedImage = i}
                  class="w-16 h-16 flex-shrink-0 rounded-lg overflow-hidden border-2 transition-colors {selectedImage === i ? 'border-[var(--color-primary)]' : 'border-[var(--color-border)]'}"
                >
                  <img src={media.url} alt="" class="w-full h-full object-cover" />
                </button>
              {/each}
            </div>
          {/if}
        </div>

        <!-- Product Info -->
        <div>
          <!-- Merchant info -->
          {#if product.merchant}
            <a 
              href="/merchant/{product.merchant.code}"
              class="inline-flex items-center gap-2 text-xs text-[var(--color-primary)] hover:underline mb-2"
            >
              {#if product.merchant.iconUrl}
                <img src={product.merchant.iconUrl} alt="" class="w-5 h-5 rounded-full object-cover" />
              {:else}
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                </svg>
              {/if}
              {product.merchant.name}
              {#if product.merchant.rating}
                <span class="text-[var(--color-text-muted)]">★ {product.merchant.rating.toFixed(1)}</span>
              {/if}
            </a>
          {/if}

          <h1 class="text-xl font-semibold text-[var(--color-text)] mb-2">
            {product.title}
          </h1>

          <!-- Brand & Category tags -->
          <div class="flex items-center gap-2 mb-4">
            {#if product.brand}
              <span class="badge badge-outline text-xs">{product.brand.name}</span>
            {/if}
            {#if product.category}
              <span class="badge badge-outline text-xs">{product.category.name}</span>
            {/if}
          </div>

          <!-- Price -->
          <div class="mb-4">
            <span class="text-2xl font-bold text-[var(--color-text)]">
              {formatPrice(selectedVariant?.price)}
            </span>
          </div>

          <!-- Stock Status -->
          <div class="mb-4">
            {#if selectedVariant?.stockInfo?.hasStock !== false && product.hasStock !== false}
              <span class="badge badge-success">In Stock ({selectedVariant?.stockInfo?.totalStock || product.totalStock || 0} available)</span>
            {:else}
              <span class="badge badge-error">Out of Stock</span>
            {/if}
          </div>

          <!-- Variant Selector -->
          {#if product.variants && product.variants.length > 1}
            <div class="mb-6">
              <h3 class="text-sm font-medium text-[var(--color-text)] mb-2">Variants</h3>
              <div class="flex flex-wrap gap-2">
                {#each product.variants as variant}
                  <button
                    onclick={() => selectVariant(variant)}
                    class="px-3 py-1.5 text-sm border rounded-lg transition-colors {selectedVariant?.subSku === variant.subSku ? 'border-[var(--color-primary)] bg-[var(--color-primary)] text-white' : 'border-[var(--color-border)] hover:border-[var(--color-primary)]'}"
                    class:opacity-50={!variant.stockInfo?.hasStock}
                  >
                    {variant.title}
                  </button>
                {/each}
              </div>
            </div>
          {/if}

          <!-- Description -->
          {#if product.shortDescription}
            <div class="mb-6">
              <h3 class="text-sm font-medium text-[var(--color-text)] mb-2">Description</h3>
              <p class="text-sm text-[var(--color-text-light)] leading-relaxed">
                {product.shortDescription}
              </p>
            </div>
          {/if}

          <!-- Tags -->
          {#if product.tags && product.tags.length > 0}
            <div class="mb-6">
              <div class="flex flex-wrap gap-1">
                {#each product.tags as tag}
                  <span class="text-xs px-2 py-0.5 bg-[var(--color-bg)] text-[var(--color-text-muted)] rounded-full">
                    #{tag}
                  </span>
                {/each}
              </div>
            </div>
          {/if}

          <!-- Quantity & Add to Cart -->
          <div class="flex items-center gap-3 mb-4">
            <div class="flex items-center border border-[var(--color-border)] rounded-lg">
              <button 
                onclick={() => quantity > 1 && (quantity -= 1)}
                class="w-9 h-9 flex items-center justify-center hover:bg-[var(--color-bg)] transition-colors"
                aria-label="Decrease quantity"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4" />
                </svg>
              </button>
              <span class="w-10 text-center font-medium text-sm">{quantity}</span>
              <button 
                onclick={() => quantity += 1}
                class="w-9 h-9 flex items-center justify-center hover:bg-[var(--color-bg)] transition-colors"
                aria-label="Increase quantity"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                </svg>
              </button>
            </div>

            <button 
              onclick={addToCart}
              disabled={!selectedVariant?.stockInfo?.hasStock && !product.hasStock}
              class="btn btn-primary flex-1 py-2.5 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
              Add to Cart
            </button>
          </div>

          <p class="text-xs text-[var(--color-text-muted)]">
            SKU: {selectedVariant?.subSku || product.sku || 'N/A'}
          </p>
        </div>
      </div>
    </div>

    <!-- Related Products -->
    {#if relatedProducts.length > 0}
      <div class="bg-white py-10 mt-6">
        <div class="container">
          <h2 class="text-lg font-semibold text-[var(--color-text)] mb-4">
            Related Products
          </h2>
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
            {#each relatedProducts as product, i}
              <ProductCard {product} index={i} />
            {/each}
          </div>
        </div>
      </div>
    {/if}
  {/if}
</div>
