package ProductService.ProductService.service;

import ProductService.ProductService.dto.ProductRequestDto;
import ProductService.ProductService.dto.ProductResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    ProductResponseDto addProduct (ProductRequestDto productRequestDto);
    ProductResponseDto getProductById(String id);
    Page<ProductResponseDto> getProducts(int page, int size);
    Page<ProductResponseDto> searchProducts(String keyword, int page, int size);
    ProductResponseDto updateProduct(String id, ProductRequestDto dto);
    void deleteProduct(String id);

    String generateBulkProducts(int count);

}
