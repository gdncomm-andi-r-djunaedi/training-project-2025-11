package com.gdn.marketplace.product.grpc;

import com.gdn.marketplace.product.entity.Product;
import com.gdn.marketplace.product.service.ProductService;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductGrpcServiceTest {

    @Mock
    private ProductService productService;

    @Mock
    private StreamObserver<ProductResponse> productResponseObserver;

    @Mock
    private StreamObserver<SearchProductsResponse> searchProductsResponseObserver;

    @InjectMocks
    private ProductGrpcService productGrpcService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId("1");
        product.setName("Test Product");
        product.setPrice(100.0);
    }

    @Test
    void getProduct_Success() {
        GetProductRequest request = GetProductRequest.newBuilder().setId("1").build();
        when(productService.getProduct("1")).thenReturn(product);

        productGrpcService.getProduct(request, productResponseObserver);

        verify(productResponseObserver).onNext(any(ProductResponse.class));
        verify(productResponseObserver).onCompleted();
    }

    @Test
    void createProduct_Success() {
        CreateProductRequest request = CreateProductRequest.newBuilder()
                .setName("Test Product")
                .setPrice(100.0)
                .build();
        when(productService.createProduct(any(Product.class))).thenReturn(product);

        productGrpcService.createProduct(request, productResponseObserver);

        verify(productResponseObserver).onNext(any(ProductResponse.class));
        verify(productResponseObserver).onCompleted();
    }

    @Test
    void searchProducts_Success() {
        SearchProductsRequest request = SearchProductsRequest.newBuilder().setQuery("Test").build();
        com.gdn.marketplace.product.document.ProductDocument doc = new com.gdn.marketplace.product.document.ProductDocument();
        doc.setId("1");
        doc.setName("Test Product");
        doc.setPrice(100.0);

        when(productService.searchProducts("Test")).thenReturn(Collections.singletonList(doc));

        productGrpcService.searchProducts(request, searchProductsResponseObserver);

        verify(searchProductsResponseObserver).onNext(any(SearchProductsResponse.class));
        verify(searchProductsResponseObserver).onCompleted();
    }
}
