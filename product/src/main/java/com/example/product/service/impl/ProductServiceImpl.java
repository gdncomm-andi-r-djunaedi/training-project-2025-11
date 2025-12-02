package com.example.product.service.impl;

import com.example.product.dto.ProductRequestDTO;
import com.example.product.dto.ProductResponseDTO;
import com.example.product.entity.Product;
import com.example.product.exceptions.ProductNotFoundException;
import com.example.product.repository.ProductRepository;
import com.example.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {

        Product product = Product.builder()
                .title(productRequestDTO.getTitle())
                .description(productRequestDTO.getDescription())
                .price(productRequestDTO.getPrice())
                .imageUrl(productRequestDTO.getImageUrl())
                .category(productRequestDTO.getCategory())
                .build();

        Product savedProduct = productRepository.save(product);
        return mapToResponseDTO(savedProduct);
    }

    @Override
    public ProductResponseDTO getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        return mapToResponseDTO(product);
    }

    @Override
    public ProductResponseDTO getProductByProductId(long productId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        return mapToResponseDTO(product);
    }

    @Override
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDTO> getProductsByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDTO> searchProductsByTitle(String title) {
        return productRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponseDTO updateProduct(String id, ProductRequestDTO updateProductDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        productRepository.save(product);
        return mapToResponseDTO(product);
    }

    @Override
    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        productRepository.delete(product);
    }

    @Override
    public void deleteProductByProductId(long id) {
        Product product = productRepository.findByProductId(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        productRepository.delete(product);
    }

    private ProductResponseDTO mapToResponseDTO(Product product) {
        return ProductResponseDTO.builder()
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .category(product.getCategory())
                .build();
    }
}
