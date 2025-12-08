package ProductService.ProductService.mapper;

import ProductService.ProductService.dto.ProductRequestDto;
import ProductService.ProductService.dto.ProductResponseDto;
import ProductService.ProductService.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public Product toEntity(ProductRequestDto dto) {
        Product product = new Product();
        product.generateId();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCreatedAt(java.time.LocalDate.now());
        return product;
    }

    public ProductResponseDto toDto(Product product) {
        ProductResponseDto resp = new ProductResponseDto();
        resp.setId(product.getId());
        resp.setName(product.getName());
        resp.setDescription(product.getDescription());
        resp.setPrice(product.getPrice());
        resp.setCreatedAt(product.getCreatedAt());
        return resp;
    }
}
