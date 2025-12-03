package org.edmund.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.edmund.commonlibrary.response.GenericResponse;
import org.edmund.product.dto.AddProductDto;
import org.edmund.product.response.AddProductResponse;
import org.edmund.product.response.GetProductListResponse;
import org.edmund.product.response.ProductDetailResponse;
import org.edmund.product.services.ProductService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping("/add")
    @Operation(summary = "Add Product")
    public GenericResponse<AddProductResponse> addProduct(@RequestBody AddProductDto request) {
        try {
            AddProductResponse createdProduct = productService.saveProduct(request);
            return GenericResponse.ok(createdProduct);
        } catch (Exception e) {
            return GenericResponse.badRequest(e.getMessage());
        }
    }

    @GetMapping("/search/product/list")
    @Operation(summary = "Get Product")
    public GenericResponse<GetProductListResponse> getProductList(@RequestParam(required = false) String name,
                                                                  @RequestParam(required = false) Integer page,
                                                                  @RequestParam(required = false) Integer size)
    {
        try {
            GetProductListResponse listProduct = productService.getListProduct(name, page, size);
            return GenericResponse.ok(listProduct);
        } catch (Exception e) {
            return GenericResponse.badRequest(e.getMessage());
        }
    }

    @GetMapping("/search/product/sku")
    @Operation(summary = "Get Product by SKU")
    public GenericResponse<ProductDetailResponse> getProductList(@RequestParam String sku)
    {
        try {
            ProductDetailResponse product = productService.getProductDetail(sku);
            return GenericResponse.ok(product);
        } catch (Exception e) {
            return GenericResponse.badRequest(e.getMessage());
        }
    }
}