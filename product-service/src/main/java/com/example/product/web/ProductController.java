package com.example.product.web;

import com.example.product.domain.Product;
import com.example.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductRepository repo;

  @GetMapping
  public Page<Product> list(@RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "20") int size,
      @RequestParam(name = "q", required = false) String q) {

    var pageable = PageRequest.of(page, size);

    if (q == null || q.isBlank()) {
      return repo.findAll(pageable);
    }
    return repo.findByNameContainingIgnoreCase(q, pageable);
  }

  @GetMapping("/{id}")
  public Product detail(@PathVariable("id") String id) {
    return repo.findById(id).orElseThrow();
  }
}