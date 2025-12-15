package project_product.product.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project_product.product.entity.Product;
import project_product.product.model.ProductRequest;
import project_product.product.model.ProductResponse;
import project_product.product.service.ProductService;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/searchProduct")
    public ResponseEntity<Page<Product>> getProductByKeyword(Pageable pageable, String keyword) {
        Page<Product> product = productService.getProductByKeyword(pageable, keyword);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/viewProductList")
    public Page<Product> getAllProducts(Pageable pageable) {
        return productService.getAllProducts(pageable);
    }

    @GetMapping("/viewProductDetail")
    public ResponseEntity<Product> getProductById(Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping("/createProduct")
    public Product createProduct(@RequestBody ProductRequest productRequest) {
        return productService.createProduct(productRequest);
    }

    @PutMapping("/updateProductBy{id}")
    public ResponseEntity<List<ProductResponse>> updateProduct(@RequestBody ProductRequest productRequest, @PathVariable Long id) {
        Product product = productService.updateProduct(productRequest, id);
        return ResponseEntity.ok(Collections.singletonList(ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build()));
    }

    @DeleteMapping("/deleteProductBy{id}")
    public ResponseEntity<Product> deleteProductById(@PathVariable Long id) {
        productService.deleteProductById(id);
        return ResponseEntity.noContent().build();
    }

}
