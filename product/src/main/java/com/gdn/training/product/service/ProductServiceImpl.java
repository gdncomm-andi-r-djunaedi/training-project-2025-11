package com.gdn.training.product.service;

import com.gdn.training.product.dto.ProductDetailResponse;
import com.gdn.training.product.dto.ProductListRequest;
import com.gdn.training.product.dto.SearchProductRequest;
import com.gdn.training.product.entity.Product;
import com.gdn.training.product.exception.ProductNotFoundException;
import com.gdn.training.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductDetailResponse viewDetailById(String productId) {
        Product product = productRepository.viewProductDetail(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        ProductDetailResponse response = new ProductDetailResponse();
        response.setProductId(product.getProduct_id());
        response.setProductName(product.getProduct_name());
        response.setPrice(product.getPrice());
        response.setDescription(product.getDescription());
        return response;
    }

    @Override
    public Page<Product> viewProductList(ProductListRequest request) {
        PageRequest paging = PageRequest.of(request.getPage(), request.getSize());

        return productRepository.findByFilters(
                request.getProductId(),
                request.getProductName(),
                paging);
    }

    @Override
    public Page<Product> searchProduct(SearchProductRequest request) {
        PageRequest paging = PageRequest.of(request.getPage(), request.getSize());
        String productName = request.getProductName() != null ? request.getProductName() : "";
        return productRepository.searchByName(productName, paging);
    }
}
