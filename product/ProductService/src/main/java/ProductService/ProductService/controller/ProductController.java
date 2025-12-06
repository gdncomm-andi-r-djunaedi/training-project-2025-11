package ProductService.ProductService.controller;

import ProductService.ProductService.common.ApiResponse;
import ProductService.ProductService.common.ResponseUtil;
import ProductService.ProductService.dto.ProductClientDto;
import ProductService.ProductService.dto.ProductRequestDto;
import ProductService.ProductService.dto.ProductResponseDto;
import ProductService.ProductService.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    ProductService productService;

    @PostMapping("/addProduct")
    public ResponseEntity<ApiResponse<?>> addProduct (@RequestBody ProductRequestDto productRequestDto){
        return ResponseEntity.ok(
                ResponseUtil.success(productService.addProduct(productRequestDto))
        );
    }

    @GetMapping("/viewProductById/{id}")
    public ResponseEntity<ApiResponse<?>> getProductById(@PathVariable String id){
        return ResponseEntity.ok(
                ResponseUtil.success(productService.getProductById(id))
        );
    }

    @GetMapping("/listProducts")
    public ResponseEntity<ApiResponse<?>> getProducts(@RequestParam(defaultValue = "0")int page,@RequestParam(defaultValue = "10")int size){
        Page<ProductResponseDto> responseDtos = productService.getProducts(page,size);
        return ResponseEntity.ok(ResponseUtil.success(responseDtos));
    }

    @GetMapping("/searchProduct")
    public ResponseEntity<ApiResponse<?>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ProductResponseDto> response = productService.searchProducts(keyword, page, size);
        return ResponseEntity.ok(ResponseUtil.success(response));
    }

    @PutMapping("/updateProduct/{id}")
    public ResponseEntity<ApiResponse<?>> updateProduct(
            @PathVariable String id,
            @RequestBody ProductRequestDto dto
    ){
        return ResponseEntity.ok(
                ResponseUtil.success(productService.updateProduct(id, dto))
        );
    }

    @DeleteMapping("/deleteProduct/{id}")
    public ResponseEntity<ApiResponse<?>> deleteProduct(@PathVariable String id){
        productService.deleteProduct(id);
        return ResponseEntity.ok(ResponseUtil.success("Product deleted successfully"));
    }

    @PostMapping("/generateBulkProducts")
    public ResponseEntity<ApiResponse<?>> generateBulkProducts(@RequestParam(defaultValue = "50000") int count) {
        String message = productService.generateBulkProducts(count);
        return ResponseEntity.ok(ResponseUtil.success(message));
    }

    // Feign API specifically for CartService
    @GetMapping("/cart/viewProductById/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProductForCart(@PathVariable String id){
        ProductResponseDto product = productService.getProductById(id);

        // Map only the fields needed by CartService
        ProductResponseDto clientResponse = new ProductResponseDto();
        clientResponse.setId(product.getId());
        clientResponse.setName(product.getName());
        clientResponse.setPrice(product.getPrice());

        return ResponseEntity.ok(ResponseUtil.success(clientResponse));
    }


}
