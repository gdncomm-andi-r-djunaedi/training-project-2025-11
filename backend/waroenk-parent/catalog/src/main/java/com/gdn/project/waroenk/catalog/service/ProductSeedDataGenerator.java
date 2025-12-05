package com.gdn.project.waroenk.catalog.service;

import com.gdn.project.waroenk.catalog.entity.Brand;
import com.gdn.project.waroenk.catalog.entity.Category;
import com.gdn.project.waroenk.catalog.entity.Inventory;
import com.gdn.project.waroenk.catalog.entity.Merchant;
import com.gdn.project.waroenk.catalog.entity.Product;
import com.gdn.project.waroenk.catalog.entity.Variant;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Service to generate realistic product seed data.
 * Uses DataFaker for realistic product names and descriptions.
 * Uses Unsplash for real product images.
 * Also handles TypeSense indexing for merchants and products.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSeedDataGenerator {

  private static final int BATCH_SIZE = 100;       // how many futures waited together
  private static final int THREADS = 4;            // tune based on CPU
  // Unsplash collection IDs for different categories
  private static final Map<String, List<String>> CATEGORY_IMAGE_KEYWORDS =
      Map.ofEntries(Map.entry("cat-smartphones", List.of("smartphone", "iphone", "android-phone")),
          Map.entry("cat-laptops", List.of("laptop", "macbook", "computer")),
          Map.entry("cat-tablets", List.of("tablet", "ipad", "digital-tablet")),
          Map.entry("cat-cameras", List.of("camera", "dslr", "photography")),
          Map.entry("cat-audio", List.of("headphones", "earbuds", "speaker")),
          Map.entry("cat-wearables", List.of("smartwatch", "fitness-tracker", "wearable")),
          Map.entry("cat-gaming", List.of("gaming", "controller", "console")),
          Map.entry("cat-accessories", List.of("tech-accessories", "charger", "cable")),
          Map.entry("cat-smart-home", List.of("smart-home", "iot", "home-automation")),
          Map.entry("cat-mens-clothing", List.of("mens-fashion", "mens-clothing", "shirt")),
          Map.entry("cat-womens-clothing", List.of("womens-fashion", "dress", "womens-clothing")),
          Map.entry("cat-shoes", List.of("shoes", "sneakers", "footwear")),
          Map.entry("cat-bags", List.of("bag", "backpack", "luggage")),
          Map.entry("cat-watches", List.of("watch", "wristwatch", "luxury-watch")),
          Map.entry("cat-jewelry", List.of("jewelry", "necklace", "ring")),
          Map.entry("cat-sportswear", List.of("sportswear", "activewear", "fitness-clothing")),
          Map.entry("cat-furniture", List.of("furniture", "sofa", "chair")),
          Map.entry("cat-kitchen", List.of("kitchen", "cookware", "kitchenware")),
          Map.entry("cat-bedding", List.of("bedding", "pillow", "bedroom")),
          Map.entry("cat-decor", List.of("home-decor", "interior", "decoration")),
          Map.entry("cat-lighting", List.of("lamp", "lighting", "chandelier")),
          Map.entry("cat-skincare", List.of("skincare", "beauty", "cosmetics")),
          Map.entry("cat-makeup", List.of("makeup", "lipstick", "cosmetics")),
          Map.entry("cat-haircare", List.of("haircare", "shampoo", "hair")),
          Map.entry("cat-fragrances", List.of("perfume", "fragrance", "cologne")),
          Map.entry("cat-exercise", List.of("gym", "fitness", "exercise")),
          Map.entry("cat-camping", List.of("camping", "outdoor", "tent")),
          Map.entry("cat-cycling", List.of("bicycle", "cycling", "bike")),
          Map.entry("cat-action-figures", List.of("action-figure", "toy", "collectible")),
          Map.entry("cat-board-games", List.of("board-game", "puzzle", "game")),
          Map.entry("cat-building-blocks", List.of("lego", "blocks", "building-toys")),
          Map.entry("cat-car-parts", List.of("car-parts", "automotive", "auto")),
          Map.entry("cat-snacks", List.of("snacks", "food", "chips")),
          Map.entry("cat-beverages", List.of("drinks", "beverage", "bottle")),
          Map.entry("cat-coffee-tea", List.of("coffee", "tea", "cafe")),
          Map.entry("cat-fiction", List.of("book", "novel", "reading")),
          Map.entry("cat-baby-gear", List.of("baby", "stroller", "baby-gear")),
          Map.entry("cat-diapers", List.of("baby-care", "diaper", "baby")),
          Map.entry("cat-kids-toys", List.of("kids-toys", "children", "play")));
  // Product templates by category
  private static final Map<String, List<ProductTemplate>> CATEGORY_TEMPLATES = new HashMap<>();

  static {
    CATEGORY_TEMPLATES.put("cat-smartphones",
        List.of(new ProductTemplate("Pro Max Smartphone",
                "Experience the future of mobile technology with our flagship smartphone. Featuring a stunning display, powerful processor, and advanced camera system that captures every moment in stunning detail.",
                List.of("Black", "White", "Blue", "Gold")),
            new ProductTemplate("Ultra Slim Phone",
                "Sleek design meets powerful performance. This ultra-slim smartphone delivers an exceptional experience with its edge-to-edge display and lightning-fast charging.",
                List.of("Midnight", "Silver", "Rose Gold")),
            new ProductTemplate("Budget-Friendly Smartphone",
                "Get all the essential features without breaking the bank. Perfect for everyday use with reliable performance and a long-lasting battery.",
                List.of("Black", "Blue", "Green"))));

    CATEGORY_TEMPLATES.put("cat-laptops",
        List.of(new ProductTemplate("Professional Laptop",
                "Designed for professionals who demand the best. Powerful processor, ample storage, and a brilliant display make this laptop perfect for any task.",
                List.of("Space Gray", "Silver")),
            new ProductTemplate("Gaming Laptop",
                "Dominate every game with this high-performance gaming laptop. RGB keyboard, powerful graphics, and advanced cooling system for marathon gaming sessions.",
                List.of("Black", "RGB Black")),
            new ProductTemplate("Ultrabook",
                "Ultra-thin, ultra-light, ultra-powerful. This premium ultrabook goes wherever you go without compromising on performance.",
                List.of("Platinum", "Graphite"))));

    CATEGORY_TEMPLATES.put("cat-audio",
        List.of(new ProductTemplate("Wireless Noise-Cancelling Headphones",
                "Immerse yourself in pure sound with industry-leading noise cancellation. Premium comfort for all-day listening with up to 30 hours of battery life.",
                List.of("Black", "White", "Navy")),
            new ProductTemplate("True Wireless Earbuds",
                "Freedom in sound. These compact earbuds deliver rich, detailed audio with a secure fit for any activity.",
                List.of("Black", "White", "Pink")),
            new ProductTemplate("Portable Bluetooth Speaker",
                "Big sound in a compact package. Waterproof design and 20-hour battery life make it perfect for any adventure.",
                List.of("Black", "Blue", "Red", "Teal"))));

    CATEGORY_TEMPLATES.put("cat-mens-clothing",
        List.of(new ProductTemplate("Premium Cotton T-Shirt",
                "Crafted from 100% organic cotton for ultimate comfort. Classic fit with reinforced stitching for lasting durability.",
                List.of("White", "Black", "Navy", "Gray", "Olive")),
            new ProductTemplate("Slim Fit Chinos",
                "Versatile and stylish, these slim-fit chinos transition seamlessly from office to weekend. Premium stretch fabric for all-day comfort.",
                List.of("Khaki", "Navy", "Black", "Olive")),
            new ProductTemplate("Casual Button-Down Shirt",
                "Effortless style for any occasion. Breathable fabric and modern fit make this shirt a wardrobe essential.",
                List.of("White", "Blue", "Pink", "Striped"))));

    CATEGORY_TEMPLATES.put("cat-womens-clothing",
        List.of(new ProductTemplate("Floral Maxi Dress",
                "Elegant and flowing, this maxi dress features a stunning floral print perfect for any special occasion or casual day out.",
                List.of("Blue Floral", "Pink Floral", "Black Floral")),
            new ProductTemplate("High-Waisted Jeans",
                "The perfect fit for every body. These high-waisted jeans combine style and comfort with premium stretch denim.",
                List.of("Light Wash", "Dark Wash", "Black")),
            new ProductTemplate("Cozy Knit Sweater",
                "Wrap yourself in warmth with this soft knit sweater. Relaxed fit and ribbed details add effortless style.",
                List.of("Cream", "Dusty Rose", "Sage", "Charcoal"))));

    CATEGORY_TEMPLATES.put("cat-shoes",
        List.of(new ProductTemplate("Running Shoes",
                "Engineered for performance with responsive cushioning and breathable mesh upper. Your new personal best awaits.",
                List.of("Black/White", "Blue/Orange", "Gray/Neon")),
            new ProductTemplate("Classic Leather Sneakers",
                "Timeless style meets modern comfort. Premium leather construction with cushioned insole for all-day wear.",
                List.of("White", "Black", "Tan")),
            new ProductTemplate("Casual Slip-On Loafers",
                "Easy elegance in every step. Soft leather and memory foam insole make these loafers incredibly comfortable.",
                List.of("Brown", "Navy", "Black"))));

    CATEGORY_TEMPLATES.put("cat-skincare",
        List.of(new ProductTemplate("Hydrating Facial Serum",
                "Intensive hydration powered by hyaluronic acid. This lightweight serum penetrates deep to plump and rejuvenate your skin.",
                List.of("30ml", "50ml")),
            new ProductTemplate("Anti-Aging Night Cream",
                "Wake up to younger-looking skin. This rich night cream with retinol and peptides works while you sleep.",
                List.of("50g")),
            new ProductTemplate("Gentle Cleansing Foam",
                "Remove impurities without stripping your skin. This pH-balanced foam cleanses gently while maintaining skin's natural moisture.",
                List.of("150ml", "200ml"))));

    CATEGORY_TEMPLATES.put("cat-watches",
        List.of(new ProductTemplate("Classic Automatic Watch",
                "Precision craftsmanship in a timeless design. Swiss movement and sapphire crystal combine heritage with innovation.",
                List.of("Silver/White", "Gold/Black", "Rose Gold/Navy")),
            new ProductTemplate("Sport Chronograph",
                "Built for athletes. Water-resistant to 100m with stopwatch function and luminous hands for any condition.",
                List.of("Black", "Blue", "Orange")),
            new ProductTemplate("Minimalist Quartz Watch",
                "Less is more. Clean lines and quality materials create a watch that complements any style.",
                List.of("Silver/White", "Black/Black", "Gold/Brown"))));

    // Add more categories as needed...
  }

  private final MongoTemplate mongoTemplate;
  private final SearchService searchService;
  private final Faker faker = new Faker();
  private final Random random = new Random(42);
  private final ExecutorService executor = Executors.newFixedThreadPool(THREADS, runnable -> {
    Thread t = new Thread(runnable);
    t.setName("typesense-indexer-" + t.getId());
    t.setDaemon(true);
    return t;
  });

  /**
   * Generate products for a specific merchant
   */
  public GenerationResult generateProductsForMerchant(String merchantCode, int productCount) {
    Merchant merchant =
        mongoTemplate.findOne(org.springframework.data.mongodb.core.query.Query.query(org.springframework.data.mongodb.core.query.Criteria.where(
            "code").is(merchantCode)), Merchant.class);

    if (merchant == null) {
      log.warn("Merchant not found: {}", merchantCode);
      return GenerationResult.builder()
          .productsCreated(0)
          .variantsCreated(0)
          .inventoriesCreated(0)
          .merchantCode(merchantCode)
          .build();
    }

    // Delete existing products, variants, and inventories for this merchant
    log.info("Cleaning existing data for merchant: {}", merchantCode);
    deleteExistingDataForMerchant(merchantCode);

    List<Category> categories = mongoTemplate.findAll(Category.class);
    List<Brand> brands = mongoTemplate.findAll(Brand.class);

    // Filter to only sub-categories
    List<Category> subCategories = categories.stream().filter(c -> c.getParentId() != null).toList();

    if (subCategories.isEmpty()) {
      subCategories = categories;
    }

    int productsCreated = 0;
    int variantsCreated = 0;
    int inventoriesCreated = 0;
    Instant now = Instant.now();

    for (int i = 0; i < productCount; i++) {
      Category category = subCategories.get(random.nextInt(subCategories.size()));
      Brand brand = brands.get(random.nextInt(brands.size()));

      ProductData productData = generateProductData(category, brand, merchantCode, i);

      Product product = Product.builder()
          .id(UUID.randomUUID().toString())
          .title(productData.title)
          .sku(productData.sku)
          .merchantCode(merchantCode)
          .categoryId(category.getId())
          .brandId(brand.getId())
          .summary(Product.ProductSummary.builder()
              .shortDescription(productData.shortDescription)
              .tags(productData.tags)
              .build())
          .detailRef("details/" + productData.sku)
          .createdAt(now)
          .updatedAt(now)
          .build();

      mongoTemplate.insert(product);
      productsCreated++;

      // Generate 1-3 variants
      int variantCount = random.nextInt(3) + 1;
      double basePrice = 50000 + random.nextDouble() * 4950000;

      for (int v = 0; v < variantCount; v++) {
        Variant variant = generateVariant(productData, v, variantCount, basePrice, now);
        mongoTemplate.insert(variant);
        variantsCreated++;

        // Create inventory for this variant
        Inventory inventory = Inventory.builder()
            .id(UUID.randomUUID().toString())
            .subSku(variant.getSubSku())
            .stock((long) (10 + random.nextInt(991)))  // Random stock 10-1000
            .createdAt(now)
            .updatedAt(now)
            .build();
        mongoTemplate.insert(inventory);
        inventoriesCreated++;
      }
    }

    return GenerationResult.builder()
        .productsCreated(productsCreated)
        .variantsCreated(variantsCreated)
        .inventoriesCreated(inventoriesCreated)
        .merchantCode(merchantCode)
        .build();
  }

  /**
   * Delete existing products, variants, and inventories for a merchant
   */
  private void deleteExistingDataForMerchant(String merchantCode) {
    // Find all products for this merchant
    var productQuery =
        org.springframework.data.mongodb.core.query.Query.query(org.springframework.data.mongodb.core.query.Criteria.where(
            "merchantCode").is(merchantCode));
    List<Product> existingProducts = mongoTemplate.find(productQuery, Product.class);

    if (existingProducts.isEmpty()) {
      log.info("No existing products for merchant: {}", merchantCode);
      return;
    }

    // Get all SKUs
    List<String> skus = existingProducts.stream().map(Product::getSku).toList();

    // Delete variants by SKU
    var variantQuery =
        org.springframework.data.mongodb.core.query.Query.query(org.springframework.data.mongodb.core.query.Criteria.where(
            "sku").in(skus));
    List<Variant> existingVariants = mongoTemplate.find(variantQuery, Variant.class);
    List<String> subSkus = existingVariants.stream().map(Variant::getSubSku).toList();

    // Delete inventories by subSku
    if (!subSkus.isEmpty()) {
      var inventoryQuery =
          org.springframework.data.mongodb.core.query.Query.query(org.springframework.data.mongodb.core.query.Criteria.where(
              "subSku").in(subSkus));
      var inventoryResult = mongoTemplate.remove(inventoryQuery, Inventory.class);
      log.info("Deleted {} inventories for merchant: {}", inventoryResult.getDeletedCount(), merchantCode);
    }

    // Delete variants
    var variantResult = mongoTemplate.remove(variantQuery, Variant.class);
    log.info("Deleted {} variants for merchant: {}", variantResult.getDeletedCount(), merchantCode);

    // Delete products
    var productResult = mongoTemplate.remove(productQuery, Product.class);
    log.info("Deleted {} products for merchant: {}", productResult.getDeletedCount(), merchantCode);
  }

  /**
   * Async version - Generate products for a specific merchant
   */
  @Async
  public CompletableFuture<Void> generateProductsForMerchantAsync(String merchantCode, int productCount) {
    log.info("=== ASYNC SEED START: Merchant {} with {} products ===", merchantCode, productCount);
    long startTime = System.currentTimeMillis();

    try {
      GenerationResult result = generateProductsForMerchant(merchantCode, productCount);
      long duration = System.currentTimeMillis() - startTime;

      log.info("=== ASYNC SEED COMPLETE: Merchant {} - {} products, {} variants, {} inventories in {}ms ===",
          merchantCode,
          result.getProductsCreated(),
          result.getVariantsCreated(),
          result.getInventoriesCreated(),
          duration);
    } catch (Exception e) {
      log.error("=== ASYNC SEED FAILED: Merchant {} - {} ===", merchantCode, e.getMessage(), e);
    }

    return CompletableFuture.completedFuture(null);
  }

  /**
   * Async version - Generate products for all merchants
   */
  @Async
  public CompletableFuture<Void> generateProductsForAllMerchantsAsync(int productsPerMerchant,
      int batchStart,
      int batchEnd) {
    log.info("=== ASYNC SEED ALL START: {} products per merchant, batch {}-{} ===",
        productsPerMerchant,
        batchStart,
        batchEnd);
    long startTime = System.currentTimeMillis();

    try {
      List<Merchant> merchants = mongoTemplate.findAll(Merchant.class);
      int startIndex = Math.max(0, batchStart - 1);
      int endIndex = Math.min(merchants.size(), batchEnd);

      int totalProducts = 0;
      int totalVariants = 0;
      int totalInventories = 0;

      for (int i = startIndex; i < endIndex; i++) {
        Merchant merchant = merchants.get(i);
        log.info("Processing merchant {}/{}: {} ({})", i + 1, endIndex, merchant.getName(), merchant.getCode());

        GenerationResult result = generateProductsForMerchant(merchant.getCode(), productsPerMerchant);

        totalProducts += result.getProductsCreated();
        totalVariants += result.getVariantsCreated();
        totalInventories += result.getInventoriesCreated();

        log.info("  -> Created {} products, {} variants, {} inventories",
            result.getProductsCreated(),
            result.getVariantsCreated(),
            result.getInventoriesCreated());
      }

      long duration = System.currentTimeMillis() - startTime;
      log.info("=== ASYNC SEED ALL COMPLETE: {} merchants, {} products, {} variants, {} inventories in {}ms ===",
          endIndex - startIndex,
          totalProducts,
          totalVariants,
          totalInventories,
          duration);

    } catch (Exception e) {
      log.error("=== ASYNC SEED ALL FAILED: {} ===", e.getMessage(), e);
    }

    return CompletableFuture.completedFuture(null);
  }

  /**
   * Index all merchants in TypeSense
   */
  public int indexAllMerchantsInTypeSense() {
    log.info("Starting TypeSense indexing for all merchants...");
    List<Merchant> merchants = mongoTemplate.findAll(Merchant.class);
    int indexed = 0;
    int failed = 0;

    for (Merchant merchant : merchants) {
      try {
        searchService.indexMerchant(merchant);
        indexed++;
      } catch (Exception e) {
        log.warn("Failed to index merchant {}: {}", merchant.getCode(), e.getMessage());
        failed++;
      }
    }

    log.info("TypeSense merchant indexing complete: {} indexed, {} failed", indexed, failed);
    return indexed;
  }

  /**
   * Async version - Index all merchants in TypeSense
   */
  @Async
  public CompletableFuture<Void> indexAllMerchantsInTypeSenseAsync() {
    log.info("=== ASYNC TYPESENSE INDEX START: Indexing all merchants ===");
    long startTime = System.currentTimeMillis();

    try {
      int indexed = indexAllMerchantsInTypeSense();
      long duration = System.currentTimeMillis() - startTime;
      log.info("=== ASYNC TYPESENSE INDEX COMPLETE: {} merchants indexed in {}ms ===", indexed, duration);
    } catch (Exception e) {
      log.error("=== ASYNC TYPESENSE INDEX FAILED: {} ===", e.getMessage(), e);
    }

    return CompletableFuture.completedFuture(null);
  }

  public int indexAllProductsInTypeSense() {
    log.info("🔥 Starting parallel TypeSense reindex...");

    AtomicInteger indexed = new AtomicInteger();
    AtomicInteger failed = new AtomicInteger();

    Query query = new Query();
    List<CompletableFuture<Void>> tasks = new ArrayList<>();
    try (Stream<Product> stream = mongoTemplate.stream(query, Product.class)) {
      stream.forEach(product -> {
        CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
          try {
            var aggregatedProducts = searchService.buildAggregatedProduct(product.getSku());

            if (aggregatedProducts != null) {
              aggregatedProducts.forEach(data -> {
                try {
                  searchService.indexProduct(data);
                  indexed.incrementAndGet();
                } catch (Exception ignored) {
                  log.warn("Product {}: index might not be complete", product.getSku());
                  failed.incrementAndGet();
                }
              });
            }

          } catch (Exception e) {
            failed.incrementAndGet();
            log.error("Failed to index product {}: {}", product.getSku(), e.getMessage());
          }

          if ((indexed.get() + failed.get()) % 1000 == 0) {
            log.info("Progress: {} indexed | {} failed", indexed.get(), failed.get());
          }

        }, executor);

        tasks.add(task);
      });
    }

    log.info("🏁 Completed TypeSense indexing: {} indexed ✔ | {} failed ❌", indexed.get(), failed.get());
    return indexed.get();
  }

  /**
   * Async version - Index all products in TypeSense
   */
  @Async
  public CompletableFuture<Void> indexAllProductsInTypeSenseAsync() {
    log.info("=== ASYNC TYPESENSE INDEX START: Indexing all products ===");
    long startTime = System.currentTimeMillis();

    try {
      int indexed = indexAllProductsInTypeSense();
      long duration = System.currentTimeMillis() - startTime;
      log.info("=== ASYNC TYPESENSE INDEX COMPLETE: {} products indexed in {}ms ===", indexed, duration);
    } catch (Exception e) {
      log.error("=== ASYNC TYPESENSE INDEX FAILED: {} ===", e.getMessage(), e);
    }

    return CompletableFuture.completedFuture(null);
  }

  private ProductData generateProductData(Category category, Brand brand, String merchantCode, int index) {
    String sku = merchantCode + "-" + String.format("%05d", index + 1);
    String categoryId = category.getId();

    // Get templates for category or use default
    List<ProductTemplate> templates = CATEGORY_TEMPLATES.getOrDefault(categoryId, getDefaultTemplates());
    ProductTemplate template = templates.get(random.nextInt(templates.size()));

    // Generate title with brand
    String title = brand.getName() + " " + template.name;

    // Generate description
    String shortDescription = template.description;

    // Get image keyword
    List<String> keywords = CATEGORY_IMAGE_KEYWORDS.getOrDefault(categoryId, List.of("product"));
    String keyword = keywords.get(random.nextInt(keywords.size()));

    // Generate tags
    List<String> tags = new ArrayList<>();
    tags.add(category.getName().toLowerCase());
    tags.add(brand.getName().toLowerCase());
    tags.addAll(generateTags(category.getName()));

    return ProductData.builder()
        .sku(sku)
        .title(title)
        .shortDescription(shortDescription)
        .longDescription(generateLongDescription(template, brand))
        .tags(tags)
        .colors(template.colors)
        .imageKeyword(keyword)
        .build();
  }

  private List<ProductTemplate> getDefaultTemplates() {
    return List.of(new ProductTemplate("Premium Product",
            "High-quality product designed to meet your everyday needs. Crafted with care using premium materials for lasting satisfaction.",
            List.of("Black", "White", "Blue")),
        new ProductTemplate("Essential Item",
            "A must-have for every household. Reliable quality and excellent value make this an essential addition to your collection.",
            List.of("Standard", "Deluxe")),
        new ProductTemplate("Professional Grade",
            "Built for professionals who demand the best. Superior quality and performance you can rely on.",
            List.of("Pro Black", "Pro Silver")));
  }

  private String generateLongDescription(ProductTemplate template, Brand brand) {
    String sb = "<h3>About This Product</h3>\n" + "<p>" + template.description + "</p>\n\n" + "<h3>Key Features</h3>\n"
        + "<ul>\n" + "<li>Premium quality from " + brand.getName() + "</li>\n"
        + "<li>Designed for durability and performance</li>\n" + "<li>Easy to use and maintain</li>\n"
        + "<li>Backed by manufacturer warranty</li>\n" + "</ul>\n\n" + "<h3>Specifications</h3>\n"
        + "<p>Please refer to the variant details for specific measurements and specifications.</p>\n";
    return sb;
  }

  private List<String> generateTags(String categoryName) {
    List<String> tags = new ArrayList<>();
    String[] words = categoryName.toLowerCase().split("[\\s&]+");
    for (String word : words) {
      if (word.length() > 2) {
        tags.add(word);
      }
    }
    // Add some random common tags
    List<String> commonTags = List.of("bestseller", "new-arrival", "premium", "popular", "trending", "sale");
    if (random.nextBoolean()) {
      tags.add(commonTags.get(random.nextInt(commonTags.size())));
    }
    return tags.stream().distinct().limit(5).toList();
  }

  private Variant generateVariant(ProductData productData,
      int variantIndex,
      int totalVariants,
      double basePrice,
      Instant now) {
    String color = productData.colors.get(variantIndex % productData.colors.size());
    String subSku = productData.sku + "-V" + (variantIndex + 1);

    String variantTitle = productData.title + " - " + color;

    // Price variation
    double priceMultiplier = 1.0 + (variantIndex * 0.1);
    double price = Math.round(basePrice * priceMultiplier / 100) * 100;

    // Attributes
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("color", color);

    // Add size for clothing categories
    if (productData.tags.stream()
        .anyMatch(t -> t.contains("clothing") || t.contains("shirt") || t.contains("dress") || t.contains("shoe"))) {
      List<String> sizes = List.of("XS", "S", "M", "L", "XL", "XXL");
      attributes.put("size", sizes.get(random.nextInt(sizes.size())));
    }

    // Generate image URLs using Lorem Picsum (real, resolvable images)
    // Use seeded URLs for consistent images based on SKU
    String imageSeed = subSku.replace("-", "");
    String thumbnail = String.format("https://picsum.photos/seed/%s/400/400", imageSeed);

    // Different seeds for different angles/views
    List<Variant.VariantMedia> media = List.of(Variant.VariantMedia.builder()
            .url(String.format("https://picsum.photos/seed/%s/800/800", imageSeed))
            .type("image")
            .sortOrder(0)
            .altText(variantTitle + " - Main Image")
            .build(),
        Variant.VariantMedia.builder()
            .url(String.format("https://picsum.photos/seed/%s-detail/800/800", imageSeed))
            .type("image")
            .sortOrder(1)
            .altText(variantTitle + " - Detail Image")
            .build(),
        Variant.VariantMedia.builder()
            .url(String.format("https://picsum.photos/seed/%s-side/800/800", imageSeed))
            .type("image")
            .sortOrder(2)
            .altText(variantTitle + " - Side View")
            .build());

    return Variant.builder()
        .id(UUID.randomUUID().toString())
        .sku(productData.sku)
        .subSku(subSku)
        .title(variantTitle)
        .price(price)
        .isDefault(variantIndex == 0)
        .attributes(attributes)
        .thumbnail(thumbnail)
        .media(media)
        .createdAt(now)
        .updatedAt(now)
        .build();
  }


  @Data
  @Builder
  public static class GenerationResult {
    private int productsCreated;
    private int variantsCreated;
    private int inventoriesCreated;
    private String merchantCode;
  }


  @Data
  @Builder
  private static class ProductData {
    private String sku;
    private String title;
    private String shortDescription;
    private String longDescription;
    private List<String> tags;
    private List<String> colors;
    private String imageKeyword;
  }


  private record ProductTemplate(String name, String description, List<String> colors) {
  }
}

