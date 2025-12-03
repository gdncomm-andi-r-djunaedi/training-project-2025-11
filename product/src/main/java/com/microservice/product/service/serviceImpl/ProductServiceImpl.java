package com.microservice.product.service.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.product.dto.ProductDto;
import com.microservice.product.dto.ProductResponseDto;
import com.microservice.product.dto.ProductSearchDto;
import com.microservice.product.entity.Product;
import com.microservice.product.exception.ResourceNotFoundException;
import com.microservice.product.repository.ProductRepository;
import com.microservice.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public Page<ProductResponseDto> getProducts(Pageable pageable) {
        Page<Product> productEntities = productRepository.findAll(pageable);
        return productEntities.map(productEntity -> convertToDto(productEntity));
    }

    @Override
    public Page<ProductResponseDto> getProductsBySearch(String searchTerm, Pageable pageable) {

        Page<Product> productEntities = productRepository.findBySearchTerm(searchTerm, pageable);
        return productEntities.map(productEntity -> convertToDto(productEntity));
    }

    @Override
    public Page<ProductResponseDto> searchProducts(ProductSearchDto searchDto) {
        Pageable pageable = PageRequest.of(searchDto.getPage(), searchDto.getSize());
        Page<Product> productEntities = productRepository.findByFilters(
                searchDto.getSearchTerm(),
                searchDto.getCategory(),
                searchDto.getBrand(),
                searchDto.getMinPrice(),
                searchDto.getMaxPrice(),
                searchDto.getIsActive(),
                searchDto.getDangerousLevel(),
                searchDto.getStoreId(),
                pageable
        );
        return productEntities.map(productEntity -> convertToDto(productEntity));
    }

    @Override
    public ProductResponseDto getProductsById(Long id) {
        if(!productRepository.existsById(id)){
            throw new ResourceNotFoundException("Instructor", id);
        }
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product was not found"));
        return convertToDto(product);
    }

    @Override
    public ProductResponseDto addProduct(ProductDto productDto) {
        return convertToDto(productRepository.save(objectMapper.convertValue(productDto, Product.class)));
    }

    @Override
    public ProductResponseDto updateProduct(Long id, ProductDto productDto) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product",id));
        
        // Update the existing product entity with values from DTO
        product.setSku(productDto.getSku());
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setCategory(productDto.getCategory());
        product.setBrand(productDto.getBrand());
        product.setPrice(productDto.getPrice());
        product.setItemCode(productDto.getItemCode());
        product.setLength(productDto.getLength());
        product.setHeight(productDto.getHeight());
        product.setWidth(productDto.getWidth());
        product.setWeight(productDto.getWeight());
        product.setDangerousLevel(productDto.getDangerousLevel());
        product.setUpdatedAt(java.time.LocalDateTime.now());
        Product updatedProduct = productRepository.save(product);
        return convertToDto(updatedProduct);
    }

    @Override
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    private ProductResponseDto convertToDto(Product productEntity) {
        return objectMapper.convertValue(productEntity, ProductResponseDto.class);
    }
}
