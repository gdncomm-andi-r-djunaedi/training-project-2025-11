package com.blibi.product.serviceimpl;

import com.blibi.product.dto.ProductDTO;
import com.blibi.product.entity.Product;
import com.blibi.product.repository.ProductRepository;
import com.blibi.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product: {}", productDTO.getProductName());

        try {
            Product createdProduct = productRepository.save(getProductFromDTO(productDTO));
            log.info("Product created successfully with ID: {}", createdProduct.getProductId());
            return getProductDTO(createdProduct);
        } catch (Exception e) {
            log.error("Error creating product: {}", productDTO.getProductName(), e);
            throw e;
        }
    }

    @Override
    public Page<ProductDTO> viewProductDetailsByName(String productName, Pageable pageable) {
        log.info("Fetching product details by name: {} (page: {}, size: {})",
                productName, pageable.getPageNumber(), pageable.getPageSize());

        Page<Product> products = productRepository.findByProductName(productName, pageable);
        log.debug("Found {} products with name: {}", products.getTotalElements(), productName);

        return products.map(product -> getProductDTO(product));
    }

    @Override
    public Page<ProductDTO> searchProductByName(String productName, Pageable pageable) {
        log.info("Searching products by name: {} (page: {}, size: {})",
                productName, pageable.getPageNumber(), pageable.getPageSize());

        Page<Product> products = productRepository.searchProductByName(productName, pageable);
        log.debug("Search found {} products matching: {}", products.getTotalElements(), productName);

        return products.map(product -> getProductDTO(product));
    }

    @Override
    public Page<ProductDTO> searchProductByCategory(String category, Pageable pageable) {
        log.info("Searching products by category: {} (page: {}, size: {})",
                category, pageable.getPageNumber(), pageable.getPageSize());

        Page<Product> products = productRepository.searchProductByCategory(category, pageable);
        log.debug("Search found {} products in category: {}", products.getTotalElements(), category);

        return products.map(product -> getProductDTO(product));
    }
}
