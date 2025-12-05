package org.edmund.product.serviceimpl;

import lombok.RequiredArgsConstructor;
import org.edmund.product.dto.AddProductDto;
import org.edmund.product.entity.Product;
import org.edmund.product.repository.ProductRepository;
import org.edmund.product.response.AddProductResponse;
import org.edmund.product.response.GetProductListResponse;
import org.edmund.product.response.ProductDetailResponse;
import org.edmund.product.services.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    public AddProductResponse saveProduct(AddProductDto request) {
        String autoSku = UUID.randomUUID().toString();
        Product product = Product.builder()
                .sku(autoSku)
                .name(request.getName())
                .merchant(request.getMerchant())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .build();

        Product saved = productRepository.save(product);
        return AddProductResponse.builder()
                .sku(saved.getSku())
                .name(saved.getName())
                .merchant(saved.getMerchant())
                .description(saved.getDescription())
                .price(saved.getPrice())
                .stock(saved.getStock())
                .build();
    }

    @Override
    public GetProductListResponse getListProduct(String name, Integer page, Integer size) {
        // 1. Validasi Page & Size (Biar gak null)
        int pageValidated = (page != null && page >= 0) ? page : 0; // default 0
        int sizeValidated = (size != null && size > 0) ? size : 10; // default 10

        // 2. Buat Pageable Object
        Pageable pageable = PageRequest.of(pageValidated, sizeValidated);

        // 3. Panggil Repository (Cari & Paginate)
        Page<Product> productPage;
        if (name != null && !name.isBlank()) {
            // Kalau ada nama, cari berdasarkan nama
            productPage = productRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            // Kalau nama kosong, ambil semua
            productPage = productRepository.findAll(pageable);
        }

        // 4. Mapping: Entity -> Response DTO
        List<ProductDetailResponse> content = productPage.getContent().stream()
                .map(product -> ProductDetailResponse.builder()
                        .sku(product.getSku())
                        .name(product.getName())
                        .merchant(product.getMerchant())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .stock(product.getStock())
                        .build())
                .collect(Collectors.toList());

        // 5. Bungkus dalam Wrapper Response
        return GetProductListResponse.builder()
                .content(content)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .isLast(productPage.isLast())
                .build();
    }

    @Override
    public ProductDetailResponse getProductDetail(String sku) {
        // 1. Cari & Validasi
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Product not found with SKU: " + sku));

        // 2. Mapping ke Response DTO
        return ProductDetailResponse.builder()
                .sku(product.getSku())
                .name(product.getName())
                .merchant(product.getMerchant())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .build();
    }
}
