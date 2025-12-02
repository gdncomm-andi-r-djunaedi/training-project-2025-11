package com.blibi.product.serviceimpl;

import com.blibi.product.dto.ProductDTO;
import com.blibi.product.entity.Product;
import com.blibi.product.repository.ProductRepository;
import com.blibi.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static com.blibi.product.utils.CommonUtils.getProductDTO;
import static com.blibi.product.utils.CommonUtils.getProductFromDTO;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product createdProduct = productRepository.save(getProductFromDTO(productDTO));
        return getProductDTO(createdProduct);
    }

    @Override
    public Page<ProductDTO> viewProductDetailsByName(String productName, Pageable pageable) {
        Page<Product> products = productRepository.findByProductName(productName, pageable);
        return products.map(product -> getProductDTO(product));
    }

    @Override
    public Page<ProductDTO> searchProductByName(String productName, Pageable pageable) {
        Page<Product> products = productRepository.searchProductByName(productName, pageable);
        return products.map(product -> getProductDTO(product));
    }

    @Override
    public Page<ProductDTO> searchProductByCategory(String category, Pageable pageable) {
        Page<Product> products = productRepository.searchProductByCategory(category, pageable);
        return products.map(product -> getProductDTO(product));
    }
}
