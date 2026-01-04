package CartService.CartService.service.client;

import CartService.CartService.dto.ProductClientResponse;
import CartService.CartService.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "ProductService",
        url = "http://localhost:8081/products"
)

public interface ProductClient {

    @GetMapping("/cart/viewProductById/{id}")
    ApiResponse<ProductClientResponse> validateProduct(@PathVariable("id") String id);
}

