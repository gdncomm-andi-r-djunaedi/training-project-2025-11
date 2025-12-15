package com.gdn.faurihakim.product.web.model;

import com.blibli.oss.backend.command.loom.executor.CommandExecutor;
import com.blibli.oss.backend.common.helper.ResponseHelper;
import com.blibli.oss.backend.common.model.response.Response;
import com.gdn.faurihakim.product.command.CreateProductCommand;
import com.gdn.faurihakim.product.command.GetProductCommand;
import com.gdn.faurihakim.product.command.SearchProductCommand;
import com.gdn.faurihakim.product.command.UpdateProductCommand;
import com.gdn.faurihakim.product.command.model.CreateProductCommandRequest;
import com.gdn.faurihakim.product.command.model.GetProductCommandRequest;
import com.gdn.faurihakim.product.command.model.SearchProductCommandRequest;
import com.gdn.faurihakim.product.command.model.UpdateProductCommandRequest;
import com.gdn.faurihakim.product.web.model.request.CreateProductWebRequest;
import com.gdn.faurihakim.product.web.model.request.UpdateProductWebRequest;
import com.gdn.faurihakim.product.web.model.response.CreateProductWebResponse;
import com.gdn.faurihakim.product.web.model.response.GetProductWebResponse;
import com.gdn.faurihakim.product.web.model.response.SearchProductWebResponse;
import com.gdn.faurihakim.product.web.model.response.UpdateProductWebResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping(value = "/api")
public class ProductController {

    @Autowired
    private CommandExecutor executor;

    @Operation(summary = "Get products with pagination and search")
    @GetMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<SearchProductWebResponse> getProducts(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Receive get products API with query: {}, page: {}, size: {}", query, page, size);

        SearchProductWebResponse response = executor.execute(
                SearchProductCommand.class, SearchProductCommandRequest.builder()
                        .query(query)
                        .page(page)
                        .size(size)
                        .build());

        return ResponseHelper.ok(response);
    }

    @Operation(summary = "Get product by product name")
    @GetMapping(value = "/products-by-name", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<GetProductWebResponse> getProductByProductName(String productName) {
        log.info("Receive get product API by product name");

        GetProductWebResponse response = executor.execute(
                GetProductCommand.class, GetProductCommandRequest.builder()
                        .productName(productName)
                        .build());

        return ResponseHelper.ok(response);
    }

    @Operation(summary = "Create product")
    @PostMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response<CreateProductWebResponse> createProduct(
            @RequestBody CreateProductWebRequest requestBody) {
        log.info("Receive create product API");

        CreateProductCommandRequest commandRequest = new CreateProductCommandRequest();
        BeanUtils.copyProperties(requestBody, commandRequest);
        CreateProductWebResponse response = executor.execute(CreateProductCommand.class, commandRequest);
        return ResponseHelper.ok(response);
    }

    @Operation(summary = "Get product by product id")
    @GetMapping(value = "/products/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<GetProductWebResponse> getProduct(@PathVariable String productId) {
        log.info("Receive get product API");

        GetProductWebResponse response = executor.execute(
                GetProductCommand.class, GetProductCommandRequest.builder()
                        .productId(productId)
                        .build());

        return ResponseHelper.ok(response);
    }

    @Operation(summary = "Update product")
    @PutMapping(value = "/products/{productId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response<UpdateProductWebResponse> updateProduct(
            @PathVariable String productId,
            @RequestBody UpdateProductWebRequest requestBody) {
        log.info("Receive update product API");
        UpdateProductCommandRequest commandRequest = new UpdateProductCommandRequest();
        BeanUtils.copyProperties(requestBody, commandRequest);
        commandRequest.setProductId(productId);
        UpdateProductWebResponse response = executor.execute(UpdateProductCommand.class, commandRequest);
        return ResponseHelper.ok(response);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Object> handleProductNotFound(ProductNotFoundException ex) {
        log.error("Product not found: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Product Not Found");
        body.put("message", ex.getMessage());
        body.put("timestamp", LocalDateTime.now().toString());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
        log.error("General exception caught: {} - Message: {}", ex.getClass().getName(), ex.getMessage());

        // Attempt to unwrap if it's a wrapper exception
        if (ex.getCause() instanceof ProductNotFoundException) {
            return handleProductNotFound((ProductNotFoundException) ex.getCause());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("error", "Internal Server Error");
        body.put("message", ex.getMessage());
        body.put("exception_type", ex.getClass().getName()); // For debugging
        body.put("timestamp", LocalDateTime.now().toString());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
