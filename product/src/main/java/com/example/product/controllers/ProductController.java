package com.example.product.controllers;

import com.example.product.dto.request.ProductDTO;
import com.example.product.dto.response.GenericResponseListDTO;
import com.example.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
@Tag(name = "Product", description = "Product search and retrieval APIs")
public class ProductController {
    private final ProductService productService;

    @Operation(
            summary = "Search products by name",
            description = "Retrieves a paginated list of products matching the given product name. " +
                    "The search is case-insensitive and supports partial matching. " +
                    "Results are returned in a paginated format based on the startIndex and size parameters."
    )
    @ApiResponses(value = {
            @ApiResponse( responseCode = "200", description = "Successfully retrieved products",
                    content = @Content(schema = @Schema(implementation = GenericResponseListDTO.class)) ),
            @ApiResponse( responseCode = "400", description = "Invalid request parameters" ),
            @ApiResponse( responseCode = "500", description = "Internal server error" )
    })
    @GetMapping("/getByName")
    public GenericResponseListDTO<ProductDTO> searchByProductName(
            @Parameter( description = "Product name to search for (case-insensitive, supports partial matching)",
                    required = true, example = "laptop" )
            @RequestParam String productName,
            @Parameter( description = "Zero-based page index (starting position for pagination)", required = true, example = "0" )
            @RequestParam int startIndex,
            @Parameter( description = "Number of items per page (page size)", required = true, example = "10" )
            @RequestParam int size) {

        log.debug("searchByProductName:: productName - {}, startIndex - {}, size - {}"
                , productName, startIndex, size);
        Pageable pageable = PageRequest.of(startIndex, size);
        return new GenericResponseListDTO<>(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                productService.searchProducts(productName, pageable)
        );

    }

    @Operation(
            summary = "Search products by name and category",
            description = "Retrieves a paginated list of products matching both the given product name and category. " +
                    "Product name search is case-insensitive and supports partial matching. " +
                    "Category filter is case-insensitive and requires exact match. " +
                    "Results are returned in a paginated format based on the startIndex and size parameters."
    )
    @ApiResponses(value = {
            @ApiResponse( responseCode = "200", description = "Successfully retrieved products",
                    content = @Content(schema = @Schema(implementation = GenericResponseListDTO.class))
            ),
            @ApiResponse( responseCode = "400", description = "Invalid request parameters" ),
            @ApiResponse( responseCode = "500", description = "Internal server error" )
    })
    @GetMapping("/getByNameAndCategory")
    public GenericResponseListDTO<ProductDTO> searchByProductNameAndCategory(
            @Parameter( description = "Product name to search for (case-insensitive, supports partial matching)",
                    required = true, example = "laptop" )
            @RequestParam String productName,
            @Parameter( description = "Product category to filter by (case-insensitive, exact match required)",
                    required = true, example = "electronics" )
            @RequestParam String category,
            @Parameter( description = "Zero-based page index (starting position for pagination)",
                    required = true, example = "0" )
            @RequestParam int startIndex,
            @Parameter( description = "Number of items per page (page size)", required = true, example = "10" )
            @RequestParam int size) {

        log.debug("searchByProductName:: productName - {}, category - {}, startIndex - {}, size - {}"
                , productName, category, startIndex, size);
        Pageable pageable = PageRequest.of(startIndex, size);
        return new GenericResponseListDTO<>(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                productService.searchProducts(productName, category, pageable)
        );

    }

}
