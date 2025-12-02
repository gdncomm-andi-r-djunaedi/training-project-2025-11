package com.example.product.service.impl;

import com.example.product.dto.GetBulkProductResponseDTO;
import com.example.product.dto.ProductRequestDTO;
import com.example.product.dto.ProductResponseDTO;
import com.example.product.entity.Product;
import com.example.product.exceptions.ProductNotFoundException;
import com.example.product.repository.ProductRepository;
import com.example.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final SequenceGeneratorService sequenceGeneratorService;

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {

        long nextId = sequenceGeneratorService.getNextSequence("product_sequence");
        Product product = Product.builder()
                .productId(nextId)
                .title(productRequestDTO.getTitle())
                .description(productRequestDTO.getDescription())
                .price(productRequestDTO.getPrice())
                .imageUrl(productRequestDTO.getImageUrl())
                .category(productRequestDTO.getCategory())
                .markForDelete(false)
                .build();

        Product savedProduct = productRepository.save(product);
        return mapToResponseDTO(savedProduct);
    }

//    @Override
//    public ProductResponseDTO getProductById(String id) {
//        Product product = productRepository.findById(id)
//                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
//        return mapToResponseDTO(product);
//    }

    @Override
    @Cacheable(value = "products", key = "#productId")
    public ProductResponseDTO getProductByProductId(long productId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        return mapToResponseDTO(product);
    }

//    @Override
//    public List<ProductResponseDTO> getAllProducts() {
//        return productRepository.findAll().stream()
//                .map(this::mapToResponseDTO)
//                .collect(Collectors.toList());
//    }

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
    @CachePut(value = "products", key = "#productId")
    public ProductResponseDTO updateProduct(long productId, ProductRequestDTO updateProductDTO) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        if(product.isMarkForDelete())
            throw new RuntimeException("cannot update the deleted product");
        Product updatedProduct = Product.builder()
                .title(updateProductDTO.getTitle())
                .description(updateProductDTO.getDescription())
                .price(updateProductDTO.getPrice())
                .imageUrl(updateProductDTO.getImageUrl())
                .category(updateProductDTO.getCategory())
                .markForDelete(false)
                .build();

        Product savedProduct = productRepository.save(updatedProduct);
        return mapToResponseDTO(savedProduct);
    }

//    @Override
//    public void deleteProduct(String id) {
//        Product product = productRepository.findById(id)
//                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
//        productRepository.delete(product);
//    }

    @Override
    @CacheEvict(value = "products", key = "#productId")
    public void deleteProductByProductId(long productId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        product.setMarkForDelete(true);
        productRepository.save(product);
    }

    @Override
    public List<GetBulkProductResponseDTO> getProductsInBulk(List<Long> productIds) {
        return productRepository.findByProductIdIn(productIds).stream().map(this::mapToBulkResponseDTO).collect(Collectors.toList());
    }

    private ProductResponseDTO mapToResponseDTO(Product product) {
        return ProductResponseDTO.builder()
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .category(product.getCategory())
                .markForDelete(product.isMarkForDelete())
                .build();
    }

    private GetBulkProductResponseDTO mapToBulkResponseDTO(Product product) {
        return GetBulkProductResponseDTO.builder()
                .productId(product.getProductId())
                .title(product.getTitle())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .markForDelete(product.isMarkForDelete())
                .build();
    }
}
