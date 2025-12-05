package com.training.productService.productmongo.controller;

import com.training.productService.productmongo.dto.ProductDTO;
import com.training.productService.productmongo.dto.ProductPageResponse;
import com.training.productService.productmongo.model.ApiResponse;
import com.training.productService.productmongo.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
@Api(tags = "Product Search API")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @PostMapping("/search")
    @ApiOperation(value = "Search products with pagination", notes = "Returns paginated list of products based on search term")
    public ResponseEntity<ProductPageResponse> getProducts(
            @ApiParam(value = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,
            @ApiParam(value = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
            @ApiParam(value = "Search term for filtering products") @RequestParam(required = false) String searchTerm) throws Exception {
        ProductPageResponse response = productService.searchProducts(searchTerm, page, size);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/createProduct")
    @ApiOperation(value = "Create a new product", notes = "Creates a new product with the provided details and returns the created product")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@RequestBody ProductDTO request) {

        ProductDTO createdProduct = productService.createProduct(request);
        ApiResponse<ProductDTO> response = ApiResponse.success(createdProduct, "Product created successfully");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/getProduct/{sku}")
    @ApiOperation(value = "Get product details by sku", notes = "Returns complete product details including SKU, name, description, price, category, tags, and images")
    public ResponseEntity<ProductDTO> getProductById(@ApiParam(value = "Product sku", required = true) @PathVariable String sku)
    {
        ProductDTO product = productService.getProductBySku(sku);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @DeleteMapping("/deleteProduct/{sku}")
    @ApiOperation(value = "Delete product by sku", notes = "Deletes the product with the specified SKU from the database")
    public ResponseEntity<ApiResponse> deleteProductBySku(@ApiParam(value = "Product sku", required = true) @PathVariable String sku)
    {
        productService.deleteProductBySku(sku);
        ApiResponse<String> response = ApiResponse.success(null, "Product deleted successfully");
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

}
