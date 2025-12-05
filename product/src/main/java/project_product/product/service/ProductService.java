package project_product.product.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import project_product.product.entity.Product;
import project_product.product.model.ProductRequest;
import project_product.product.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Get All Product
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    // Get Product By Keyword
    public Page<Product> getProductByKeyword(Pageable pageable, String keyword) {
        return productRepository.findAllByKeyword(pageable, keyword);
    }

    // Get Product By Id
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // Create Product
    public Product createProduct(ProductRequest productRequest) {
        return productRepository.save(Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .build());
    }

    // Delete Product By Id
    public void deleteProductById(Long id) {
        productRepository.deleteById(id);
    }

    // Update Product
    public Product updateProduct(ProductRequest productRequest, Long id) {
        Product product = productRepository.
                findById(id).
                orElseThrow(() -> new RuntimeException("Product not found"));
        product.setId(id);
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        return productRepository.save(product);
    }
}
