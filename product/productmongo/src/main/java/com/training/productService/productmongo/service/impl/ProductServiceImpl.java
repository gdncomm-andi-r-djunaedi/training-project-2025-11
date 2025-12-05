package com.training.productService.productmongo.service.impl;

import com.training.productService.productmongo.dto.ProductDTO;
import com.training.productService.productmongo.dto.ProductPageResponse;
import com.training.productService.productmongo.entity.Product;
import com.training.productService.productmongo.exception.ProductServiceException;
import com.training.productService.productmongo.repository.ProductRepository;
import com.training.productService.productmongo.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductPageResponse searchProducts(String searchTerm, int page, int size) throws Exception {
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }
        if (searchTerm == null || searchTerm.isEmpty()) {
            throw ProductServiceException.invalidPayload();
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.searchProducts(searchTerm, pageable);
        if (productPage.isEmpty() && searchTerm != null && !searchTerm.trim().isEmpty()) {
            throw ProductServiceException.notFound("Product not found with the search term");
        }
        List<ProductDTO> productDTOs = productPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        ProductPageResponse response = new ProductPageResponse();
        response.setContent(productDTOs);
        response.setPageable(new ProductPageResponse.PageableInfo(page, size));
        response.setTotalElements(productPage.getTotalElements());
        response.setTotalPages(productPage.getTotalPages());
        response.setLast(productPage.isLast());
        return response;
    }

    @Override
    public ProductDTO getProductBySku(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("Product sku cannot be null or empty");
        }
        Product product = (Product) productRepository.findBySku(sku).orElseThrow(() -> ProductServiceException.notFound("Product not found in the system"));
        return convertToDTO(product);
    }

    @Override
    public ProductDTO createProduct(ProductDTO request) {
        validateCreateProductRequest(request);
        if (productRepository.findBySku(request.getSku()).isPresent()) {
            throw ProductServiceException.duplicateSku(request.getSku());
        }
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setSku(request.getSku());
        product.setCategory(request.getCategory());
        product.setTags(request.getTags());
        product.setImages(request.getImages());
        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    @Override
    public void deleteProductBySku(String sku)
    {
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("Product sku cannot be null or empty");
        }
        Product product = (Product) productRepository.findBySku(sku).orElseThrow(() -> ProductServiceException.notFound("Product not found in the system"));
        productRepository.delete(product);
    }

    private void validateCreateProductRequest(ProductDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Product request cannot be null");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new IllegalArgumentException("Product price must be greater than 0");
        }
        if (request.getSku() == null || request.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("Product SKU is required");
        }

    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setSku(product.getSku());
        dto.setCategory(product.getCategory());
        dto.setTags(product.getTags());
        dto.setImages(product.getImages());
        return dto;
    }
}
