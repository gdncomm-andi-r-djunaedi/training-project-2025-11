package com.training.marketplace.cart.service;

import com.training.marketplace.cart.client.ProductClientTestService;
import com.training.marketplace.cart.entity.CartEntity;
import com.training.marketplace.cart.entity.ProductEntity;
import com.training.marketplace.cart.modal.request.AddProductToCartRequest;
import com.training.marketplace.cart.modal.response.DefaultCartResponse;
import com.training.marketplace.cart.repository.CartRepository;
import com.training.marketplace.product.controller.modal.request.GetProductDetailRequest;
import com.training.marketplace.product.controller.modal.request.GetProductDetailResponse;
import com.training.marketplace.product.entity.Product;
import com.training.marketplace.product.service.ProductServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "grpc.server.inProcessName=test",
        "grpc.server.port=-1",
        "grpc.client.inProcess.address=in-process:test"
})
public class CartServiceImplTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartServiceImpl cartService;

    private AddProductToCartRequest addProductToCartRequest;
    private GetProductDetailResponse productDetailResponse;
    private StreamObserver<DefaultCartResponse> responseObserver;
    private ProductServiceGrpc.ProductServiceImplBase mockProductServer;
    private ProductClientTestService productClientTestService;
    private Server inProcessServer;
    private ManagedChannel managedChannel;


    @BeforeEach
    void setUp() throws IOException {
        addProductToCartRequest = AddProductToCartRequest.newBuilder()
                .setUserId("testUser")
                .setProductId("testProduct")
                .setQuantity(1)
                .build();

        productDetailResponse = GetProductDetailResponse.newBuilder()
                .setProduct(Product.newBuilder()
                        .setProductId("testProduct")
                        .setProductName("Test Product")
                        .setProductPrice(100)
                        .setProductImage("test_image.jpg")
                        .build())
                .build();

        String serverName = InProcessServerBuilder.generateName();
        mockProductServer = spy(ProductServiceGrpc.ProductServiceImplBase.class);

        inProcessServer = InProcessServerBuilder
                .forName(serverName)
                .directExecutor()
                .addService(mockProductServer)
                .build()
                .start();

        managedChannel = InProcessChannelBuilder
                .forName(serverName)
                .directExecutor()
                .usePlaintext()
                .build();

        productClientTestService = new ProductClientTestService(managedChannel);

        responseObserver = mock(StreamObserver.class);
    }

    @AfterEach
    void tearDown() {
        cartRepository.deleteAll();
    }

    @Test
    void addProductToCart_newCart() {
        mockGetProductDetail(GetProductDetailRequest.newBuilder().setProductId("TESTS-000003").build());
        cartService.addProductToCart(addProductToCartRequest, responseObserver);

        CartEntity savedCart = cartRepository.findByUserId("testUser").orElse(null);
        assertNotNull(savedCart);
        assertEquals(1, savedCart.getCartProducts().size());
        assertEquals("testProduct", savedCart.getCartProducts().get(0).getProductId());

        verify(responseObserver, times(1)).onNext(any(DefaultCartResponse.class));
        verify(responseObserver, times(1)).onCompleted();
    }

    @Test
    void addProductToCart_existingCart() {
        cartRepository.save(new CartEntity("cartId", "testUser", new ArrayList<>()));
        mockGetProductDetail(GetProductDetailRequest.newBuilder().setProductId("TESTS-000007").build());

        cartService.addProductToCart(addProductToCartRequest, responseObserver);

        CartEntity savedCart = cartRepository.findByUserId("testUser").orElse(null);
        assertNotNull(savedCart);
        assertEquals(1, savedCart.getCartProducts().size());
        assertEquals("testProduct", savedCart.getCartProducts().get(0).getProductId());

        verify(responseObserver, times(1)).onNext(any(DefaultCartResponse.class));
        verify(responseObserver, times(1)).onCompleted();
    }

    @Test
    void addProductToCart_existingProductInCart() {
        ProductEntity productEntity = ProductEntity.builder()
                .productId("testProduct")
                .productCartQuantity(1)
                .build();
        List<ProductEntity> productList = new ArrayList<>();
        productList.add(productEntity);
        cartRepository.save(new CartEntity("cartId", "testUser", productList));
        mockGetProductDetail(GetProductDetailRequest.newBuilder().setProductId("TESTS-000005").build());

        cartService.addProductToCart(addProductToCartRequest, responseObserver);

        CartEntity savedCart = cartRepository.findByUserId("testUser").orElse(null);
        assertNotNull(savedCart);
        assertEquals(1, savedCart.getCartProducts().size());
        assertEquals(2, savedCart.getCartProducts().get(0).getProductCartQuantity());

        verify(responseObserver, times(1)).onNext(any(DefaultCartResponse.class));
        verify(responseObserver, times(1)).onCompleted();
    }

    private void mockGetProductDetail(GetProductDetailRequest request){
        Mockito.doAnswer(
                invocation -> {
                    StreamObserver<GetProductDetailResponse> observer = invocation.getArgument(1);
                    GetProductDetailResponse response = GetProductDetailResponse.newBuilder()
                            .setProduct(
                                    Product.newBuilder()
                                            .setProductId(request.getProductId())
                                            .setProductName("Barang Unit Test")
                                            .setProductDetail("Detail Unit Test")
                                            .setProductImage("Image Unit Test")
                                            .build())
                            .build();

                    observer.onNext(response);
                    observer.onCompleted();
                    return null;
                }).when(mockProductServer).getProductDetail(any(), any());
    }
}
