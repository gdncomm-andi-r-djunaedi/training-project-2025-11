package com.gdn.marketplace.product.service;

import com.gdn.marketplace.product.entity.Product;
import com.gdn.marketplace.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    public Product saveProduct(Product product) {
        return repository.save(product);
    }

    public List<Product> saveProducts(List<Product> products) {
        return repository.saveAll(products);
    }

    @Cacheable(value = "products", key = "#id")
    public Product getProductById(String id) {
        return repository.findById(id).orElse(null);
    }

    public Page<Product> getProducts(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }

    public Page<Product> searchProducts(String name, int page, int size) {
        return repository.findByNameContainingIgnoreCase(name, PageRequest.of(page, size));
    }

    @CacheEvict(value = "products", key = "#id")
    public String deleteProduct(String id) {
        repository.deleteById(id);
        return "product removed !! " + id;
    }

    @CacheEvict(value = "products", key = "#product.id")
    public Product updateProduct(Product product) {
        Product existingProduct = repository.findById(product.getId()).orElse(null);
        if (existingProduct != null) {
            existingProduct.setName(product.getName());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setCategory(product.getCategory());
            return repository.save(existingProduct);
        }
        return null;
    }
}
