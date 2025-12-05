package com.gdn.training.product.service;

import com.gdn.training.product.dto.ProductListRequest;
import com.gdn.training.product.dto.SearchProductRequest;
import com.gdn.training.product.entity.Product;
import com.gdn.training.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Optional<Product> viewDetailById(String product_id) {
        return productRepository.viewProductDetail(product_id);
    }

    @Override
    public Page<Product> viewProductList(ProductListRequest request) {
        Specification<Product> spec = Specification.where(null);

        if (request.getProductId() != null && !request.getProductId().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("product_id"), request.getProductId()));
        }

        if (request.getProductName() != null && !request.getProductName().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("product_name")), "%" + request.getProductName().toLowerCase() + "%"));
        }

        PageRequest paging = PageRequest.of(request.getPage(), request.getSize());
        return productRepository.findAll(spec, paging);
    }

    @Override
    public Page<Product> searchProduct(SearchProductRequest request) {
        PageRequest paging = PageRequest.of(request.getPage(), request.getSize());
        String product_name = request.getProductName() != null ? request.getProductName() : "";
        return productRepository.searchByName(product_name, paging);
    }
}
