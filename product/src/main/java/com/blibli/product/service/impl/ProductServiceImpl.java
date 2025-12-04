package com.blibli.product.service.impl;

import com.blibli.product.dto.PageResponse;
import com.blibli.product.dto.ProductEvent;
import com.blibli.product.dto.ProductRequest;
import com.blibli.product.dto.ProductResponse;
import com.blibli.product.entity.Product;
import com.blibli.product.enums.CategoryType;
import com.blibli.product.exception.BadRequestException;
import com.blibli.product.exception.InvalidSkuFormatException;
import com.blibli.product.exception.ResourceNotFoundException;
import com.blibli.product.messaging.ProductEventProducer;
import com.blibli.product.repository.ProductRepository;
import com.blibli.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private static final String SKU_REGEX = "^[A-Za-z]{3}-\\d{5}-\\d{5}$"; // AAA-#####-#####
    private static final Pattern SKU_PATTERN = Pattern.compile(SKU_REGEX);

    private final ProductRepository productRepository;
    private final ProductEventProducer eventProducer;

    public ProductResponse createProduct(ProductRequest request) {
        String validatedSku = validateAndFormatSku(request.getSku());

        if (productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("SKU already exists");
        }
        if (productRepository.existsBySkuIgnoreCase(validatedSku)) {
            throw new BadRequestException("Product with SKU '" + validatedSku + "' already exists.");
        }

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 1000)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created: {}", saved.getId());

        // sending event to kafka
        ProductEvent event = createProductEvent(saved, "CREATE");
        eventProducer.sendProductEvent(event);

        return mapToResponse(saved);
    }

    public ProductResponse updateProduct(String id, ProductRequest request) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (StringUtils.hasText(request.getName())) {
            existingProduct.setName(request.getName().trim());
        }
        if (StringUtils.hasText(request.getDescription())) {
            existingProduct.setDescription(request.getDescription().trim());
        }

        // Check if SKU is being changed and if new SKU already exists
        if (!existingProduct.getSku().equals(request.getSku()) && productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("SKU already exists");
        }

        existingProduct.setSku(request.getSku());
        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setCategory(request.getCategory());
        existingProduct.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 1000);
        existingProduct.setUpdatedAt(LocalDateTime.now());

        Product updated = productRepository.save(existingProduct);
        log.info("Product updated: {}", updated.getId());

        // Send event to Kafka
        ProductEvent event = createProductEvent(updated, "UPDATE");
        eventProducer.sendProductEvent(event);

        return mapToResponse(updated);
    }

    public ProductResponse getProductById(String id) {
        // Try to find by MongoDB ID first
        Optional<Product> product = productRepository.findById(id);
        
        // If not found by ID, try by SKU (for user-friendly access)
        if (product.isEmpty()) {
            product = productRepository.findBySku(id);
        }
        
        // If still not found, throw exception
        Product foundProduct = product.orElseThrow(() -> 
            new ResourceNotFoundException("Product not found with id or sku: " + id));
        
        return mapToResponse(foundProduct);
    }


    public ProductResponse getProductBySku(String sku) {

        if (!StringUtils.hasText(sku)) {
            throw new IllegalArgumentException("SKU cannot be blank for lookup.");
        }

//        Optional<Product> product =  product = productRepository.findBySku(sku);

        Optional<Product> product =  product = productRepository.findBySkuIgnoreCase(sku);


        Product foundProduct = product.orElseThrow(() ->
                new ResourceNotFoundException("Product not found with id or sku: " + sku));

        return mapToResponse(foundProduct);
    }

    public PageResponse<ProductResponse> getAllProducts(int page, int size) {
        Page<Product> productPage = productRepository.findByIsActiveTrue(
                PageRequest.of(page, size));

        List<ProductResponse> content = productPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<ProductResponse>builder()
                .content(content)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .first(productPage.isFirst())
                .build();
    }

    public PageResponse<ProductResponse> getProductsByCategory(CategoryType category, int page, int size) {
        Page<Product> productPage = productRepository.findByIsActiveTrueAndCategory(
                category, PageRequest.of(page, size));

        List<ProductResponse> content = productPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<ProductResponse>builder()
                .content(content)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .first(productPage.isFirst())
                .build();
    }

    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        product.setIsActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        // Send event to Kafka
        ProductEvent event = createProductEvent(product, "DELETE");
        eventProducer.sendProductEvent(event);

        log.info("Product deleted: {}", id);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .stockQuantity(product.getStockQuantity())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private ProductEvent createProductEvent(Product product, String eventType) {
        return ProductEvent.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .stockQuantity(product.getStockQuantity())
                .eventType(eventType)
                .build();
    }

    private String validateAndFormatSku(String rawSku) {
        if (!StringUtils.hasText(rawSku)) {
            throw new IllegalArgumentException("SKU cannot be blank.");
        }

        String trimmedSku = rawSku.trim();
        Matcher matcher = SKU_PATTERN.matcher(trimmedSku);

        if (!matcher.matches()) {
            throw new InvalidSkuFormatException(rawSku);
        }
        return trimmedSku.toUpperCase();
    }
}
