package com.gdn.marketplace.product.service;

import com.gdn.marketplace.product.entity.Product;
import com.gdn.marketplace.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private com.gdn.marketplace.product.repository.ProductSearchRepository productSearchRepository;

    public Product createProduct(Product product) {
        Product savedProduct = productRepository.save(product);

        com.gdn.marketplace.product.document.ProductDocument productDocument = new com.gdn.marketplace.product.document.ProductDocument();
        productDocument.setId(savedProduct.getId());
        productDocument.setName(savedProduct.getName());
        productDocument.setDescription(savedProduct.getDescription());
        productDocument.setPrice(savedProduct.getPrice());
        productSearchRepository.save(productDocument);

        return savedProduct;
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Product getProduct(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public java.util.List<com.gdn.marketplace.product.document.ProductDocument> searchProducts(String query) {
        return productSearchRepository.findByNameContaining(query);
    }
}
