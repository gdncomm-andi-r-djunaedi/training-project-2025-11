package com.training.marketplace.gateway.service;

import com.training.marketplace.cart.CartServiceGrpc;
import com.training.marketplace.cart.modal.request.AddProductToCartRequest;
import com.training.marketplace.cart.modal.request.RemoveProductFromCartRequest;
import com.training.marketplace.cart.modal.request.ViewCartRequest;
import com.training.marketplace.cart.modal.response.DefaultCartResponse;
import com.training.marketplace.cart.modal.response.ViewCartResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.client.ImportGrpcClients;
import org.springframework.stereotype.Service;

@Service
@ImportGrpcClients(target = "cart")
public class CartClientService {

    @Autowired
    private CartServiceGrpc.CartServiceBlockingStub cartSvcStub;

    public DefaultCartResponse addProductToCart(AddProductToCartRequest request){
        return cartSvcStub.addProductToCart(request);
    }

    public ViewCartResponse viewCart(ViewCartRequest request){
        return cartSvcStub.viewCart(request);
    }

    public DefaultCartResponse removeProductFromCart(RemoveProductFromCartRequest request){
        return cartSvcStub.removeProductFromCart(request);
    }
}
