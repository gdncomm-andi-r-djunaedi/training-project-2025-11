package com.gdn.faurihakim.product.web.model;

import com.blibli.oss.backend.command.loom.executor.CommandExecutor;
import com.blibli.oss.backend.common.helper.ResponseHelper;
import com.blibli.oss.backend.common.model.response.Response;
import com.gdn.faurihakim.product.command.CreateProductCommand;
import com.gdn.faurihakim.product.command.GetProductCommand;
import com.gdn.faurihakim.product.command.UpdateProductCommand;
import com.gdn.faurihakim.product.command.model.CreateProductCommandRequest;
import com.gdn.faurihakim.product.command.model.GetProductCommandRequest;
import com.gdn.faurihakim.product.command.model.UpdateProductCommandRequest;
import com.gdn.faurihakim.product.web.model.request.CreateProductWebRequest;
import com.gdn.faurihakim.product.web.model.request.UpdateProductWebRequest;
import com.gdn.faurihakim.product.web.model.response.CreateProductWebResponse;
import com.gdn.faurihakim.product.web.model.response.GetProductWebResponse;
import com.gdn.faurihakim.product.web.model.response.UpdateProductWebResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping(value = "/api")
public class ProductController {

    @Autowired
    private CommandExecutor executor;

    @Operation(summary = "Get product")
    @GetMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<GetProductWebResponse> getProduct(String productName) {
        log.info("Receive get product API");

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
}
