package com.gdn.product.service.impl;

import com.gdn.product.dto.request.ProductDTO;
import com.gdn.product.dto.request.SearchProductDTO;
import com.gdn.product.dto.response.ProductSearchResponseDTO;
import com.gdn.product.entity.Product;
import com.gdn.product.event.ProductUpdateEvent;
import com.gdn.product.exception.InvalidSearchRequestException;
import com.gdn.product.exception.ProductNotFoundException;
import com.gdn.product.repository.ProductServiceRepository;
import com.gdn.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductServiceRepository repository;
    private final KafkaTemplate<String, ProductUpdateEvent> kafkaTemplate;

    private static final String PRODUCT_UPDATE_TOPIC = "product-update";

    public ProductServiceImpl(ProductServiceRepository repository,
                              KafkaTemplate<String, ProductUpdateEvent> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating product: {}", productDTO);

        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);

        Product savedEntity = repository.save(product);

        ProductDTO saved = new ProductDTO();
        BeanUtils.copyProperties(savedEntity, saved);

        log.info("Product created with productId={}", saved.getProductId());
        return saved;
    }

    @Override
    public ProductDTO update(ProductDTO dto) {
        log.info("Updating product, productId={}", dto.getProductId());

        Product product = repository.findByProductId(dto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + dto.getProductId()));

        BeanUtils.copyProperties(dto, product, "id");

        Product updated = repository.save(product);

        ProductUpdateEvent event = new ProductUpdateEvent(
                updated.getProductId(),
                updated.getProductName(),
                updated.getPrice(),
                updated.getCategory(),
                updated.getBrand()
        );
        kafkaTemplate.send(PRODUCT_UPDATE_TOPIC, updated.getProductId(), event);
        log.info("Published product update event for productId={}", updated.getProductId());

        ProductDTO response = new ProductDTO();
        BeanUtils.copyProperties(updated, response);
        return response;
    }

    public Product getById(String id) {
        log.info("Fetching product entity by id={}", id);

        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    }


    @Override
    public ProductSearchResponseDTO search(SearchProductDTO req) {
        log.info("Search products request: {}", req);

        if (req.getKeyword() == null || req.getKeyword().isBlank()) {
            throw new InvalidSearchRequestException("keyword is mandatory");
        }

        int page = (req.getPage() != null) ? req.getPage() : 0;
        int size = (req.getSize() != null) ? req.getSize() : 20;
        int sortCode = (req.getSort() != null) ? req.getSort() : 0;

        Sort sort = switch (sortCode) {
            case 1 -> Sort.by("price").ascending();
            case 2 -> Sort.by("price").descending();
            default -> Sort.by("productName").ascending();
        };

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> resultPage =
                repository.findByProductNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        req.getKeyword(),
                        req.getKeyword(),
                        pageable
                );

        List<ProductDTO> summaries = resultPage.getContent()
                .stream()
                .map(product -> {
                    ProductDTO dto = new ProductDTO();
                    BeanUtils.copyProperties(product, dto);
                    return dto;
                })
                .collect(Collectors.toList());

        ProductSearchResponseDTO response = new ProductSearchResponseDTO();
        response.setContent(summaries);
        response.setPage(resultPage.getNumber());
        response.setSize(resultPage.getSize());
        response.setTotalElements(resultPage.getTotalElements());
        response.setTotalPages(resultPage.getTotalPages());

        log.info("Search completed: keyword='{}', totalElements={}",
                req.getKeyword(), resultPage.getTotalElements());

        return response;
    }

    @Override
    public ProductDTO getProductDetail(String productId) {
        log.info("Fetching product detail, productId={}", productId);

        Product product = repository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        ProductDTO dto = new ProductDTO();
        BeanUtils.copyProperties(product, dto);
        return dto;
    }
}
