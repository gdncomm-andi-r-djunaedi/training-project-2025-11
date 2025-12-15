package com.training.marketplace.cart.service;

import com.training.marketplace.cart.CartServiceGrpc;
import com.training.marketplace.cart.entity.CartEntity;
import com.training.marketplace.cart.entity.ProductCart;
import com.training.marketplace.cart.entity.ProductEntity;
import com.training.marketplace.cart.modal.request.AddProductToCartRequest;
import com.training.marketplace.cart.modal.request.RemoveProductFromCartRequest;
import com.training.marketplace.cart.modal.request.ViewCartRequest;
import com.training.marketplace.cart.modal.response.DefaultCartResponse;
import com.training.marketplace.cart.modal.response.ViewCartResponse;
import com.training.marketplace.cart.repository.CartRepository;
import com.training.marketplace.product.controller.modal.request.GetProductDetailResponse;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@GrpcService
public class CartServiceImpl extends CartServiceGrpc.CartServiceImplBase {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductClientService productClient;

    @Override
    public void addProductToCart(AddProductToCartRequest request, StreamObserver<DefaultCartResponse> responseObserver) {
        String userId = request.getUserId();

        GetProductDetailResponse productDetailResponse = productClient.getProductDetail(request.getProductId());

        CartEntity cart = Optional.ofNullable(cartRepository.findByUserId(userId).orElse(null)).orElse(new CartEntity(UUID.randomUUID().toString(), userId, new ArrayList<>()));
        List<ProductEntity> productCartList = cart.getCartProducts();

        if (productCartList.stream().filter(product -> product.getProductId().equals(request.getProductId())).count() == 0) {
            productCartList.add(ProductEntity.builder()
                    .productId(productDetailResponse.getProduct().getProductId())
                    .productName(productDetailResponse.getProduct().getProductName())
                    .productPrice(productDetailResponse.getProduct().getProductPrice())
                    .productImage(productDetailResponse.getProduct().getProductImage())
                    .productCartQuantity(request.getQuantity())
                    .build());
            cart.setCartProducts(productCartList);
        } else {
            for (int i = 0; i < productCartList.size(); i++){
                if (productCartList.get(i).getProductId().equals(request.getProductId())){
                    ProductEntity updatedEntity = productCartList.get(i);
                    updatedEntity.setProductCartQuantity(updatedEntity.getProductCartQuantity() + request.getQuantity());
                    productCartList.remove(i);
                    productCartList.add(i,updatedEntity);
                }
            }
        }

        cartRepository.save(cart);

        DefaultCartResponse response = DefaultCartResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Product added to cart successfully")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void viewCart(ViewCartRequest request, StreamObserver<ViewCartResponse> responseObserver) {
        String userId = request.getUserId();
        Optional<CartEntity> cart = Optional.ofNullable(cartRepository.findByUserId(userId).get());

        List<ProductCart> productCartList = new ArrayList<>();

        if (cart.isPresent()) {
            for (ProductEntity entity : cart.get().getCartProducts()) {
                productCartList.add(
                        ProductCart.newBuilder()
                                .setProductId(entity.getProductId())
                                .setProductName(entity.getProductName())
                                .setProductPrice(entity.getProductPrice())
                                .setProductImage(entity.getProductImage())
                                .setProductCartQuantity(entity.getProductCartQuantity())
                                .build()
                );
            }
        }

        ViewCartResponse response = ViewCartResponse.newBuilder()
                .setUserId(userId)
                .addAllProducts(productCartList)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void RemoveProductFromCart(RemoveProductFromCartRequest request, StreamObserver<DefaultCartResponse> responseObserver) {
        Optional<CartEntity> cart = Optional.ofNullable(cartRepository.findByUserId(request.getUserId()).get());
        List<ProductEntity> productCartList = cart.get().getCartProducts();

        for (int i = 0; i < productCartList.size(); i++){
            if (productCartList.get(i).getProductId().equals(request.getProductId())){
                ProductEntity updatedEntity = productCartList.get(i);
                int updatedQty = updatedEntity.getProductCartQuantity() - request.getQuantity();
                updatedEntity.setProductCartQuantity(updatedQty);
                productCartList.remove(i);
                if (updatedQty > 0){
                    productCartList.add(i,updatedEntity);
                }
            }
        }

        DefaultCartResponse response = DefaultCartResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Success remove / delete product from cart")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
