package com.gdn.project.waroenk.catalog.service;

import com.gdn.project.waroenk.catalog.dto.inventory.InventoryCheckItemDto;
import com.gdn.project.waroenk.catalog.dto.merchant.MerchantResponseDto;
import com.gdn.project.waroenk.catalog.dto.product.AggregatedProductDto;
import com.gdn.project.waroenk.catalog.dto.product.ProductDetailDto;
import com.gdn.project.waroenk.catalog.dto.product.ProductDetailDto.*;
import com.gdn.project.waroenk.catalog.entity.Brand;
import com.gdn.project.waroenk.catalog.entity.Category;
import com.gdn.project.waroenk.catalog.entity.Inventory;
import com.gdn.project.waroenk.catalog.entity.Merchant;
import com.gdn.project.waroenk.catalog.entity.Product;
import com.gdn.project.waroenk.catalog.entity.Variant;
import com.gdn.project.waroenk.catalog.exceptions.ResourceNotFoundException;
import com.gdn.project.waroenk.catalog.repository.BrandRepository;
import com.gdn.project.waroenk.catalog.repository.CategoryRepository;
import com.gdn.project.waroenk.catalog.repository.InventoryRepository;
import com.gdn.project.waroenk.catalog.repository.MerchantRepository;
import com.gdn.project.waroenk.catalog.repository.ProductRepository;
import com.gdn.project.waroenk.catalog.repository.VariantRepository;
import com.gdn.project.waroenk.catalog.utility.CacheUtil;
import com.gdn.project.waroenk.catalog.utility.ParserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.typesense.model.FacetCounts;
import org.typesense.model.SearchParameters;
import org.typesense.model.SearchResult;
import org.typesense.model.SearchResultHit;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchServiceImpl implements SearchService {

  // Cache key prefixes
  private static final String MERCHANT_CACHE_PREFIX = "search:merchant:";
  private static final String BRAND_CACHE_PREFIX = "search:brand:";
  private static final String CATEGORY_CACHE_PREFIX = "search:category:";
  private static final String INVENTORY_CACHE_PREFIX = "search:inventory:";

  // Cache TTLs
  private static final int METADATA_CACHE_TTL_HOURS = 1;
  private static final int INVENTORY_CACHE_TTL_SECONDS = 30;

  private final TypeSenseService typeSenseService;
  private final ProductSearchIndexMapper productMapper;
  private final MerchantSearchIndexMapper merchantMapper;
  private final ProductRepository productRepository;
  private final VariantRepository variantRepository;
  private final MerchantRepository merchantRepository;
  private final BrandRepository brandRepository;
  private final CategoryRepository categoryRepository;
  private final InventoryRepository inventoryRepository;
  private final CacheUtil<Merchant> merchantCacheUtil;
  private final CacheUtil<Brand> brandCacheUtil;
  private final CacheUtil<Category> categoryCacheUtil;
  private final CacheUtil<Inventory> inventoryCacheUtil;
  private final Executor typesenseExecutor;

  @Value("${default.item-per-page}")
  private Integer defaultItemPerPage;

  public SearchServiceImpl(
      TypeSenseService typeSenseService,
      ProductSearchIndexMapper productMapper,
      MerchantSearchIndexMapper merchantMapper,
      ProductRepository productRepository,
      VariantRepository variantRepository,
      MerchantRepository merchantRepository,
      BrandRepository brandRepository,
      CategoryRepository categoryRepository,
      InventoryRepository inventoryRepository,
      CacheUtil<Merchant> merchantCacheUtil,
      CacheUtil<Brand> brandCacheUtil,
      CacheUtil<Category> categoryCacheUtil,
      CacheUtil<Inventory> inventoryCacheUtil,
      @Qualifier("typesenseExecutor") Executor typesenseExecutor) {
    this.typeSenseService = typeSenseService;
    this.productMapper = productMapper;
    this.merchantMapper = merchantMapper;
    this.productRepository = productRepository;
    this.variantRepository = variantRepository;
    this.merchantRepository = merchantRepository;
    this.brandRepository = brandRepository;
    this.categoryRepository = categoryRepository;
    this.inventoryRepository = inventoryRepository;
    this.merchantCacheUtil = merchantCacheUtil;
    this.brandCacheUtil = brandCacheUtil;
    this.categoryCacheUtil = categoryCacheUtil;
    this.inventoryCacheUtil = inventoryCacheUtil;
    this.typesenseExecutor = typesenseExecutor;
  }

  private PageResult toPageResult(SearchResult result, int page, int perPage) {
    List<SearchResultHit> hits = result.getHits();
    Integer totalDocuments = result.getFound();
    int totalPages = (int) Math.ceil((double) totalDocuments / perPage);

    String nextPageCursor = null;
    // Calculate if there is a next page
    if (result.getPage() < totalPages) {
      // Encode the next page number as Base64
      int nextPage = page + 1;
      nextPageCursor = encodeCursor(nextPage, perPage, null);
    }

    return new PageResult(hits,
        result.getFacetCounts(),
        hits.size(),
        totalDocuments,
        totalPages,
        result.getSearchTimeMs(),
        nextPageCursor);
  }

  private String encodeCursor(int page, int size, Map<String, String> additionalCursor) {
    StringBuilder builder = new StringBuilder();
    builder.append("p=").append(page);
    builder.append("&");
    builder.append("s=").append(size);
    if (additionalCursor != null) {
      additionalCursor.forEach((key, value) -> {
        builder.append("&");
        builder.append(key).append("=").append(value);
      });
    }

    return ParserUtil.encodeBase64(builder.toString());
  }


  Map<String, String> decodeCursor(String cursor, Set<String> keys) {
    String decoded = ParserUtil.decodeBase64(cursor);
    String[] parts = Objects.requireNonNull(decoded).split("&");

    Map<String, String> result = new HashMap<>();
    for (String part : parts) {
      String[] entry = part.split("=", 2);
      result.put(entry[0], entry.length > 1 ? entry[1] : "");
    }

    return result;
  }

  @Override
  public Result<AggregatedProductDto> searchProducts(String query,
      int size,
      String cursor,
      String sortBy,
      String sortOrder,
      Boolean buyable) throws Exception {
    int page = 1;
    if (StringUtils.isNotBlank(cursor)) {
      Map<String, String> params = decodeCursor(cursor, null);
      page = Integer.parseInt(params.getOrDefault("p", "1"));
    }
    SearchParameters parameters = new SearchParameters();
    parameters.page(page).perPage(size);
    if (StringUtils.isBlank(query)) {
      parameters.q("*");
    } else {
      parameters.q(query.trim().toLowerCase());
    }
    if (buyable != null) {
      // Typesense boolean filter syntax: field:true or field:false (not field:=value)
      parameters.filterBy(String.format("inStock:%s", buyable));
    }
    if (StringUtils.isNotBlank(sortBy)) {
      sortOrder = StringUtils.isNotBlank(sortOrder) ? sortOrder : "asc";
      parameters.sortBy(String.format("%s:%s", sortBy.trim(), sortOrder.toLowerCase().trim()));
    }
    SearchResult result = typeSenseService.search(parameters, productMapper);
    PageResult pageResult = toPageResult(result, result.getPage(), size);
    List<AggregatedProductDto> contents = mapProductsParallel(result);
    return new Result<>(contents,
        pageResult.facetCounts(),
        pageResult.totalReturned(),
        pageResult.totalMatch(),
        pageResult.totalPage(),
        pageResult.took(),
        pageResult.nextToken());
  }

  @Override
  public Result<MerchantResponseDto> searchMerchants(String query,
      int size,
      String cursor,
      String sortBy,
      String sortOrder) throws Exception {
    int page = 1;
    if (StringUtils.isNotBlank(cursor)) {
      Map<String, String> params = decodeCursor(cursor, null);
      page = Integer.parseInt(params.getOrDefault("p", "1"));
    }
    SearchParameters parameters = new SearchParameters();
    parameters.page(page).perPage(size);
    if (StringUtils.isBlank(query)) {
      parameters.q("*");
    } else {
      parameters.q(query.trim().toLowerCase());
    }
    if (StringUtils.isNotBlank(sortBy)) {
      sortOrder = StringUtils.isNotBlank(sortOrder) ? sortOrder : "asc";
      parameters.sortBy(String.format("%s:%s", sortBy.trim(), sortOrder.toLowerCase().trim()));
    }
    SearchResult result = typeSenseService.search(parameters, merchantMapper);
    PageResult pageResult = toPageResult(result, result.getPage(), size);
    List<MerchantResponseDto> contents = mapMerchantsParallel(result);

    return new Result<>(contents,
        pageResult.facetCounts(),
        pageResult.totalReturned(),
        pageResult.totalMatch(),
        pageResult.totalPage(),
        pageResult.took(),
        pageResult.nextToken());
  }

  @Override
  public CompletableFuture<CombinedResult> search(String query,
      int size,
      String cursor,
      String sortBy,
      String sortDirection) {
    long startTime = System.currentTimeMillis();
    int page = 1;
    String productCursor;
    String merchantCursor;
    if (StringUtils.isNotBlank(cursor)) {
      Map<String, String> params = decodeCursor(cursor, null);
      page = Integer.parseInt(params.getOrDefault("p", "1"));
      productCursor = params.get("pd");
      merchantCursor = params.get("mc");
    } else {
      merchantCursor = null;
      productCursor = null;
    }

    final int finalPage = page;

    CompletableFuture<Result<AggregatedProductDto>> productFuture = CompletableFuture.supplyAsync(() -> {
      try {
        if (StringUtils.isNotBlank(cursor)) {
          return StringUtils.isBlank(productCursor) ?
              new Result<>(new ArrayList<>(), new ArrayList<>(), 0, 0, 0, 0, null) :
              searchProducts(query, size, productCursor, sortBy, sortDirection, null);
        } else {
          return searchProducts(query, size, null, sortBy, sortDirection, null);
        }
      } catch (Exception e) {
        log.warn("Fail to query product : {}", query, e);
        return new Result<>(new ArrayList<>(), new ArrayList<>(), 0, 0, 0, 0, null);
      }
    }, typesenseExecutor);

    CompletableFuture<Result<MerchantResponseDto>> merchantFuture = CompletableFuture.supplyAsync(() -> {
      try {
        if (StringUtils.isNotBlank(cursor)) {
          return StringUtils.isBlank(merchantCursor) ?
              new Result<>(new ArrayList<>(), new ArrayList<>(), 0, 0, 0, 0, null) :
              searchMerchants(query, size, merchantCursor, sortBy, sortDirection);
        } else {
          return searchMerchants(query, size, null, sortBy, sortDirection);
        }
      } catch (Exception e) {
        log.warn("Fail to query product : {}", query, e);
        return new Result<>(new ArrayList<>(), new ArrayList<>(), 0, 0, 0, 0, null);
      }
    }, typesenseExecutor);

    // Combine the results when both futures complete
    return productFuture.thenCombine(merchantFuture, (productResults, merchantResults) -> {

      long endTime = System.currentTimeMillis();
      long elapsedTime = endTime - startTime;
      Map<String, String> params = new HashMap<>();
      if (StringUtils.isNotBlank(productResults.nextToken())) {
        params.put("pd", productResults.nextToken());
      }
      if (StringUtils.isNotBlank(merchantResults.nextToken())) {
        params.put("mc", productResults.nextToken());
      }
      int totalReturned = productResults.totalReturned() + merchantResults.totalReturned();
      int totalMatch = productResults.totalMatch() + merchantResults.totalMatch();
      String nextToken = encodeCursor(finalPage + 1, size, params);
      return new CombinedResult(productResults, merchantResults, totalReturned, totalMatch, elapsedTime, nextToken);
    });
  }

  @Override
  public void indexProduct(AggregatedProductDto product) {
    try {
      typeSenseService.index(product, productMapper);
    } catch (Exception e) {
      log.warn("Fail to index product {} from typesense", product.subSku());
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deleteProductIndex(String productId) {
    try {
      typeSenseService.delete(productId, productMapper);
    } catch (Exception e) {
      log.warn("Fail to delete product {} from typesense", productId);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void indexMerchant(Merchant merchant) {
    try {
      typeSenseService.index(merchant, merchantMapper);
    } catch (Exception e) {
      log.warn("Fail to index merchant {} from typesense", merchant.getCode());
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deleteMerchantIndex(String merchantId) {
    try {
      typeSenseService.delete(merchantId, merchantMapper);
    } catch (Exception e) {
      log.warn("Fail to delete merchant {} from typesense", merchantId);
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<AggregatedProductDto> buildAggregatedProduct(String sku) {
    List<AggregatedProductDto> results = new ArrayList<>();
    Optional<Product> productOpt = productRepository.findBySku(sku);
    if (productOpt.isEmpty()) {
      return Collections.emptyList();
    }

    Product product = productOpt.get();
    Optional<Merchant> merchant = merchantRepository.findByCode(product.getMerchantCode());

    // Get all the variant
    Map<String, Variant> variants = variantRepository.findBySku(sku)
        .stream()
        .collect(Collectors.toMap(Variant::getSubSku, Function.identity(), (existing, duplicate) -> duplicate));
    Map<String, Inventory> inventories = inventoryRepository.findBySubSkuIn(variants.keySet().stream().toList())
        .stream()
        .collect(Collectors.toMap(Inventory::getSubSku, Function.identity(), (existing, duplicate) -> duplicate));

    String brandName = product.getBrandId() != null ?
        brandRepository.findById(product.getBrandId()).map(Brand::getName).orElse(null) :
        null;

    String categoryName = product.getCategoryId() != null ?
        categoryRepository.findById(product.getCategoryId()).map(Category::getName).orElse(null) :
        null;

    variants.forEach((key, variant) -> {
      // Build variant keywords from attributes
      List<String> variantKeywords = new ArrayList<>();
      if (variant.getAttributes() != null) {
        variant.getAttributes().values().forEach(v -> {
          if (v != null)
            variantKeywords.add(v.toString());
        });
      }
      // Check stock
      boolean inStock = inventories.get(key).getStock() > 0L;
      // Create slug from title
      String slug = product.getTitle() != null ?
          product.getTitle().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "") :
          sku.toLowerCase();

      results.add(new AggregatedProductDto(variant.getSubSku(),
          merchant.orElse(new Merchant()).getName(),
          merchant.orElse(new Merchant()).getLocation(),
          inStock,
          variant.getTitle(),
          product.getSummary() != null ? product.getSummary().getShortDescription() : null,
          brandName,
          categoryName,
          variant.getThumbnail(),
          slug,
          variant.getAttributes(),
          variantKeywords,
          product.getSku(),
          variant.getSubSku(),
          variant.getPrice(),
          variant.getCreatedAt(),
          variant.getUpdatedAt()));
    });

    return results;
  }

  /**
   * Maps search results to AggregatedProductDto using parallel CompletableFuture for performance.
   * Avoids reflection-based ObjectMapper by using direct field extraction.
   */
  private List<AggregatedProductDto> mapProductsParallel(SearchResult result) {
    if (result.getHits() == null || result.getHits().isEmpty()) {
      return List.of();
    }

    List<CompletableFuture<AggregatedProductDto>> futures = result.getHits()
        .stream()
        .map(hit -> CompletableFuture.supplyAsync(() -> mapToProduct(hit.getDocument()), typesenseExecutor))
        .toList();

    return futures.stream().map(CompletableFuture::join).filter(Objects::nonNull).toList();
  }

  /**
   * Maps search results to MerchantResponseDto using parallel CompletableFuture for performance.
   * Avoids reflection-based ObjectMapper by using direct field extraction.
   */
  private List<MerchantResponseDto> mapMerchantsParallel(SearchResult result) {
    if (result.getHits() == null || result.getHits().isEmpty()) {
      return List.of();
    }

    List<CompletableFuture<MerchantResponseDto>> futures = result.getHits()
        .stream()
        .map(hit -> CompletableFuture.supplyAsync(() -> mapToMerchant(hit.getDocument()), typesenseExecutor))
        .toList();

    return futures.stream().map(CompletableFuture::join).filter(Objects::nonNull).toList();
  }

  /**
   * Manual mapping from Typesense document to AggregatedProductDto.
   * Direct field extraction is significantly faster than reflection-based ObjectMapper.
   */
  @SuppressWarnings("unchecked")
  private AggregatedProductDto mapToProduct(Map<String, Object> doc) {
    if (doc == null)
      return null;

    return new AggregatedProductDto(getString(doc, "id"),
        getString(doc, "merchantName"),
        getString(doc, "merchantLocation"),
        getBoolean(doc, "inStock"),
        getString(doc, "title"),
        getString(doc, "body"),
        getString(doc, "brand"),
        getString(doc, "category"),
        getString(doc, "thumbnail"),
        getString(doc, "slug"),
        (Map<String, Object>) doc.get("attributes"),
        getStringList(doc, "variantKeywords"),
        getString(doc, "sku"),
        getString(doc, "subSku"),
        getDouble(doc, "price"),
        getInstant(doc, "createdAt"),
        getInstant(doc, "updatedAt"));
  }

  /**
   * Manual mapping from Typesense document to MerchantResponseDto.
   * Direct field extraction is significantly faster than reflection-based ObjectMapper.
   */
  private MerchantResponseDto mapToMerchant(Map<String, Object> doc) {
    if (doc == null)
      return null;

    MerchantResponseDto.ContactInfoDto contact = null;
    String phone = getString(doc, "phone");
    String email = getString(doc, "email");
    if (phone != null || email != null) {
      contact = new MerchantResponseDto.ContactInfoDto(phone, email);
    }

    return new MerchantResponseDto(getString(doc, "id"),
        getString(doc, "title"),
        // name is stored as title in Typesense
        getString(doc, "code"),
        getString(doc, "iconUrl"),
        getString(doc, "location"),
        contact,
        getFloat(doc, "rating"),
        getInstant(doc, "createdAt"),
        getInstant(doc, "updatedAt"));
  }

  // Helper methods for safe type extraction from Map
  private String getString(Map<String, Object> doc, String key) {
    Object value = doc.get(key);
    return value != null ? value.toString() : null;
  }

  private boolean getBoolean(Map<String, Object> doc, String key) {
    Object value = doc.get(key);
    if (value instanceof Boolean)
      return (Boolean) value;
    if (value instanceof String)
      return Boolean.parseBoolean((String) value);
    return false;
  }

  private Double getDouble(Map<String, Object> doc, String key) {
    Object value = doc.get(key);
    if (value == null)
      return null;
    if (value instanceof Number)
      return ((Number) value).doubleValue();
    try {
      return Double.parseDouble(value.toString());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private Float getFloat(Map<String, Object> doc, String key) {
    Object value = doc.get(key);
    if (value == null)
      return null;
    if (value instanceof Number)
      return ((Number) value).floatValue();
    try {
      return Float.parseFloat(value.toString());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private Instant getInstant(Map<String, Object> doc, String key) {
    Object value = doc.get(key);
    if (value == null)
      return null;
    if (value instanceof Number) {
      // Assume it's epoch millis
      return Instant.ofEpochMilli(((Number) value).longValue());
    }
    if (value instanceof String) {
      try {
        return Instant.parse((String) value);
      } catch (Exception e) {
        return null;
      }
    }
    return null;
  }

  private List<String> getStringList(Map<String, Object> doc, String key) {
    Object value = doc.get(key);
    if (value == null)
      return null;
    if (value instanceof List) {
      return ((List<?>) value).stream().filter(Objects::nonNull).map(Object::toString).toList();
    }
    return null;
  }

  record PageResult(Iterable<SearchResultHit> hits, List<FacetCounts> facetCounts, int totalReturned, int totalMatch,
                    int totalPage, int took, String nextToken) {

  }

  // ============================================================
  // New Product Details, Summary, and Inventory Check Methods
  // ============================================================

  @Override
  public ProductDetailsResult getProductDetails(String id) throws Exception {
    long startTime = System.currentTimeMillis();

    // First, try to find by subSku in Typesense for fast lookup
    SearchParameters params = new SearchParameters()
        .q("*")
        .filterBy(String.format("subSku:=%s || sku:=%s", id, id))
        .perPage(1);

    SearchResult result = typeSenseService.search(params, productMapper);

    String sku;
    if (result.getHits() != null && !result.getHits().isEmpty()) {
      Map<String, Object> doc = result.getHits().get(0).getDocument();
      sku = getString(doc, "sku");
    } else {
      // Fallback: try to find by variant subSku or product sku in DB
      Optional<Variant> variantOpt = variantRepository.findBySubSku(id);
      if (variantOpt.isPresent()) {
        sku = variantOpt.get().getSku();
      } else {
        Optional<Product> productOpt = productRepository.findBySku(id);
        if (productOpt.isPresent()) {
          sku = productOpt.get().getSku();
        } else {
          throw new ResourceNotFoundException("Product not found with id: " + id);
        }
      }
    }

    // Get the product from DB
    Product product = productRepository.findBySku(sku)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with sku: " + sku));

    // Fetch all related data with caching
    Merchant merchant = getCachedMerchant(product.getMerchantCode());
    Brand brand = product.getBrandId() != null ? getCachedBrand(product.getBrandId()) : null;
    Category category = product.getCategoryId() != null ? getCachedCategory(product.getCategoryId()) : null;

    // Get all variants for this product
    List<Variant> variants = variantRepository.findBySku(sku);
    List<String> subSkus = variants.stream().map(Variant::getSubSku).toList();

    // Get inventory for all variants with short TTL cache
    Map<String, Inventory> inventoryMap = getCachedInventories(subSkus);

    // Build the detailed response
    ProductDetailDto detailDto = buildProductDetailDto(product, merchant, brand, category, variants, inventoryMap);

    long took = System.currentTimeMillis() - startTime;
    return new ProductDetailsResult(detailDto, took);
  }

  @Override
  public ProductSummaryResult getProductSummary(List<String> subSkus) throws Exception {
    long startTime = System.currentTimeMillis();

    if (subSkus == null || subSkus.isEmpty()) {
      return new ProductSummaryResult(Collections.emptyList(), 0, 0, 0);
    }

    int totalRequested = subSkus.size();

    // Build filter for exact subSku match
    String filterBy = subSkus.stream()
        .map(sku -> "subSku:=" + sku)
        .collect(Collectors.joining(" || "));

    SearchParameters params = new SearchParameters()
        .q("*")
        .filterBy(filterBy)
        .perPage(Math.min(totalRequested, 250)); // Typesense limit

    SearchResult result = typeSenseService.search(params, productMapper);
    List<AggregatedProductDto> products = mapProductsParallel(result);

    long took = System.currentTimeMillis() - startTime;
    return new ProductSummaryResult(products, products.size(), totalRequested, took);
  }

  @Override
  public InventoryCheckResult checkInventory(List<String> subSkus) {
    long startTime = System.currentTimeMillis();

    if (subSkus == null || subSkus.isEmpty()) {
      return new InventoryCheckResult(Collections.emptyList(), 0, 0, 0);
    }

    int totalRequested = subSkus.size();

    // Get inventories with short TTL cache
    Map<String, Inventory> inventoryMap = getCachedInventories(subSkus);

    List<InventoryCheckItemDto> items = subSkus.stream()
        .filter(inventoryMap::containsKey)
        .map(subSku -> {
          Inventory inv = inventoryMap.get(subSku);
          return new InventoryCheckItemDto(
              subSku,
              inv.getStock(),
              inv.getStock() != null && inv.getStock() > 0,
              inv.getUpdatedAt()
          );
        })
        .toList();

    long took = System.currentTimeMillis() - startTime;
    return new InventoryCheckResult(items, items.size(), totalRequested, took);
  }

  // ============================================================
  // Cache Helper Methods
  // ============================================================

  private Merchant getCachedMerchant(String merchantCode) {
    if (StringUtils.isBlank(merchantCode)) return null;

    String cacheKey = MERCHANT_CACHE_PREFIX + merchantCode;
    Merchant cached = merchantCacheUtil.getValue(cacheKey);
    if (cached != null) {
      return cached;
    }

    Merchant merchant = merchantRepository.findByCode(merchantCode).orElse(null);
    if (merchant != null) {
      merchantCacheUtil.putValue(cacheKey, merchant, METADATA_CACHE_TTL_HOURS, TimeUnit.HOURS);
    }
    return merchant;
  }

  private Brand getCachedBrand(String brandId) {
    if (StringUtils.isBlank(brandId)) return null;

    String cacheKey = BRAND_CACHE_PREFIX + brandId;
    Brand cached = brandCacheUtil.getValue(cacheKey);
    if (cached != null) {
      return cached;
    }

    Brand brand = brandRepository.findById(brandId).orElse(null);
    if (brand != null) {
      brandCacheUtil.putValue(cacheKey, brand, METADATA_CACHE_TTL_HOURS, TimeUnit.HOURS);
    }
    return brand;
  }

  private Category getCachedCategory(String categoryId) {
    if (StringUtils.isBlank(categoryId)) return null;

    String cacheKey = CATEGORY_CACHE_PREFIX + categoryId;
    Category cached = categoryCacheUtil.getValue(cacheKey);
    if (cached != null) {
      return cached;
    }

    Category category = categoryRepository.findById(categoryId).orElse(null);
    if (category != null) {
      categoryCacheUtil.putValue(cacheKey, category, METADATA_CACHE_TTL_HOURS, TimeUnit.HOURS);
    }
    return category;
  }

  private Map<String, Inventory> getCachedInventories(List<String> subSkus) {
    Map<String, Inventory> result = new HashMap<>();
    List<String> missedSubSkus = new ArrayList<>();

    // Check cache first
    for (String subSku : subSkus) {
      String cacheKey = INVENTORY_CACHE_PREFIX + subSku;
      Inventory cached = inventoryCacheUtil.getValue(cacheKey);
      if (cached != null) {
        result.put(subSku, cached);
      } else {
        missedSubSkus.add(subSku);
      }
    }

    // Fetch missed from DB
    if (!missedSubSkus.isEmpty()) {
      List<Inventory> inventories = inventoryRepository.findBySubSkuIn(missedSubSkus);
      for (Inventory inv : inventories) {
        String cacheKey = INVENTORY_CACHE_PREFIX + inv.getSubSku();
        inventoryCacheUtil.putValue(cacheKey, inv, INVENTORY_CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        result.put(inv.getSubSku(), inv);
      }
    }

    return result;
  }

  // ============================================================
  // DTO Building Helper Methods
  // ============================================================

  private ProductDetailDto buildProductDetailDto(
      Product product,
      Merchant merchant,
      Brand brand,
      Category category,
      List<Variant> variants,
      Map<String, Inventory> inventoryMap) {

    // Build merchant detail
    MerchantDetailDto merchantDto = merchant != null ? new MerchantDetailDto(
        merchant.getId(),
        merchant.getName(),
        merchant.getCode(),
        merchant.getIconUrl(),
        merchant.getLocation(),
        merchant.getRating(),
        merchant.getContact() != null ?
            new MerchantDetailDto.ContactInfoDto(
                merchant.getContact().getPhone(),
                merchant.getContact().getEmail()
            ) : null
    ) : null;

    // Build brand detail
    BrandDetailDto brandDto = brand != null ? new BrandDetailDto(
        brand.getId(),
        brand.getName(),
        brand.getSlug(),
        brand.getIconUrl()
    ) : null;

    // Build category detail
    CategoryDetailDto categoryDto = category != null ? new CategoryDetailDto(
        category.getId(),
        category.getName(),
        category.getSlug(),
        category.getIconUrl(),
        category.getParentId()
    ) : null;

    // Build variant details with stock info
    List<VariantDetailDto> variantDtos = variants.stream().map(variant -> {
      Inventory inventory = inventoryMap.get(variant.getSubSku());
      VariantDetailDto.StockInfoDto stockInfo = inventory != null ?
          new VariantDetailDto.StockInfoDto(
              inventory.getStock() != null ? inventory.getStock() : 0,
              inventory.getStock() != null && inventory.getStock() > 0,
              inventory.getUpdatedAt()
          ) :
          new VariantDetailDto.StockInfoDto(0, false, null);

      List<VariantDetailDto.VariantMediaDto> mediaDtos = variant.getMedia() != null ?
          variant.getMedia().stream().map(m -> new VariantDetailDto.VariantMediaDto(
              m.getUrl(),
              m.getType(),
              m.getSortOrder(),
              m.getAltText()
          )).toList() : Collections.emptyList();

      return new VariantDetailDto(
          variant.getId(),
          variant.getSubSku(),
          variant.getTitle(),
          variant.getPrice(),
          variant.getIsDefault(),
          variant.getAttributes(),
          variant.getThumbnail(),
          mediaDtos,
          stockInfo,
          variant.getCreatedAt(),
          variant.getUpdatedAt()
      );
    }).toList();

    // Calculate total stock
    long totalStock = inventoryMap.values().stream()
        .mapToLong(inv -> inv.getStock() != null ? inv.getStock() : 0)
        .sum();
    boolean hasStock = totalStock > 0;

    // Build product summary
    String shortDescription = product.getSummary() != null ? product.getSummary().getShortDescription() : null;
    List<String> tags = product.getSummary() != null ? product.getSummary().getTags() : null;

    return new ProductDetailDto(
        product.getId(),
        product.getSku(),
        product.getTitle(),
        shortDescription,
        tags,
        product.getDetailRef(),
        merchantDto,
        brandDto,
        categoryDto,
        variantDtos,
        totalStock,
        hasStock,
        product.getCreatedAt(),
        product.getUpdatedAt()
    );
  }
}


