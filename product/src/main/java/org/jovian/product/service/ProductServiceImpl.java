package org.jovian.product.service;

import org.jovian.common.dto.ProductDTO;
import org.jovian.product.dto.ProductRequest;
import org.jovian.product.entity.Product;
import org.jovian.product.exception.ProductAlreadyExistsException;
import org.jovian.product.exception.ProductDoesntExistException;
import org.jovian.product.repository.ProductRepository;
import org.jovian.product.dto.ProductDetailsResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class ProductServiceImpl implements ProductService{

    public final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductDTO createProduct(ProductRequest productRequest) {
        String producerCode = productRequest.getProducerCode();
        String productCode = generateProductCode();
        String variationCode = generateVariationCode();

        String productid = producerCode + "-" + productCode +  "-" + variationCode;

        if (productRepository.existsById(productid)) {
            throw new ProductAlreadyExistsException();
        }

        Product product = new Product();

        product.setProducerCode(producerCode);
        product.setProducerName(productRequest.getProducerName());
        product.setProductName(productRequest.getProductName());
        product.setProductPrice(productRequest.getProductPrice());
        product.setProductCode(productCode);
        product.setVariationCode(variationCode);
        product.setId(productid);
        product.setProductCategory(productRequest.getProductCategory());

        Product savedProduct = productRepository.save(product);
        System.out.println("Product has been successfully created");
        return toDTO(savedProduct);
    }

    @Override
    public String generateProductCode() {
        int productCode = (int)(Math.random() * 100000);
        return String.format("%05d", productCode);
    }

    @Override
    public String generateVariationCode() {
        int variationCode = (int)(Math.random() * 10000);
        return String.format("%04d", variationCode);
    }

    @Override
    public ProductDetailsResponse viewProductDetails(String productId){
        Product product = productRepository.findById(productId).orElseThrow(ProductDoesntExistException::new);

        return new ProductDetailsResponse(
                product.getId(),
                product.getProductName(),
                product.getProductPrice(),
                product.getProductCategory(),
                product.getProducerName()
        );
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> searchProducts(String keyword, Pageable pageable){
        if (keyword == null || keyword.isBlank()) {
            return productRepository.findAll(pageable);
        }
        return productRepository.searchProducts(keyword, pageable);
    }

    @Override
    public ProductDTO toDTO(Product product){
        return ProductDTO.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .productCategory(product.getProductCategory())
                .productPrice(product.getProductPrice())
                .productCode(product.getProductCode())
                .variationCode(product.getVariationCode())
                .producerCode(product.getProducerCode())
                .producerName(product.getProducerName())
                .build();
    }

    @Override
    public Product findById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductDoesntExistException());
    }
}
