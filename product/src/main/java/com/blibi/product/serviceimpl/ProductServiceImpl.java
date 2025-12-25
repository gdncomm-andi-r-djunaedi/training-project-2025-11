package com.blibi.product.serviceimpl;

import com.blibi.product.dto.ProductDTO;
import com.blibi.product.entity.Product;
import com.blibi.product.repository.ProductRepository;
import com.blibi.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static com.blibi.product.utils.CommonUtils.getProductDTO;
import static com.blibi.product.utils.CommonUtils.getProductFromDTO;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

   // Product Creation Logic
    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product: {}", productDTO.getProductName());

        try {
            // Convert DTO to Entity for persistence
            // This separation allows us to have different validation rules for DTO vs Entity
            Product productEntity = getProductFromDTO(productDTO);

            // Save to MongoDB - MongoDB will auto-generate the productId if not provided
            Product createdProduct = productRepository.save(productEntity);

            // Log successful creation with generated ID
            log.info("Product created successfully with ID: {}", createdProduct.getProductId());
            // Convert Entity back to DTO for response (includes generated ID)
            return getProductDTO(createdProduct);
        } catch (Exception e) {
            // Log error with context for troubleshooting
            log.error("Error creating product: {}", productDTO.getProductName(), e);
            // Re-throw exception to be handled by controller/global exception handler
            throw e;
        }
    }

     // Retrieves products by exact product name match.

    @Override
    @Cacheable(value = "Product Name", key = "#productName")
    public Page<ProductDTO> viewProductDetailsByName(String productName, Pageable pageable) {
        // Log the search operation with pagination details
        log.info("Fetching product details by name: {} (page: {}, size: {})",
                productName, pageable.getPageNumber(), pageable.getPageSize());

        // Query repository for exact match (case-sensitive)
        Page<Product> products = productRepository.findByProductName(productName, pageable);

        // Log debug information about results (only visible in DEBUG log level)
        log.debug("Found {} products with name: {}", products.getTotalElements(), productName);

        // Convert each Product entity to ProductDTO using Stream map operation
        // This ensures we return DTOs instead of entities, maintaining layer separation
        return products.map(product -> getProductDTO(product));
    }

    //  Searches products by name using case-insensitive partial matching.

    @Override
    public Page<ProductDTO> searchProductByName(String productName, Pageable pageable) {
        // Log the search operation with pagination details
        log.info("Searching products by name: {} (page: {}, size: {})",
                productName, pageable.getPageNumber(), pageable.getPageSize());
        // Query repository for case-insensitive partial match using MongoDB regex
        Page<Product> products = productRepository.searchProductByName(productName, pageable);
        // Log debug information about search results
        log.debug("Search found {} products matching: {}", products.getTotalElements(), productName);
        // Convert each Product entity to ProductDTO
        return products.map(product -> getProductDTO(product));
    }

     // This method fetches a product using its business productId (e.g.MTA-000001)

    @Override
    public ProductDTO getProductDetail(String productId) {
        // Log the retrieval attempt
        log.info("Fetching product details for productId: {}", productId);
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with productId: " + productId));
        return getProductDTO(product);
    }

    //  Searches products by category using case-insensitive partial matching.

    @Override

    public Page<ProductDTO> searchProductByCategory(String category, Pageable pageable) {
        // Log the category search operation with pagination details
        log.info("Searching products by category: {} (page: {}, size: {})",
                category, pageable.getPageNumber(), pageable.getPageSize());
        // Query repository for case-insensitive category match using MongoDB regex
        Page<Product> products = productRepository.searchProductByCategory(category, pageable);

        // Log debug information about category search results
        log.debug("Search found {} products in category: {}", products.getTotalElements(), category);

        // Convert each Product entity to ProductDTO
        return products.map(product -> getProductDTO(product));
    }
}
