package com.gdn.project.waroenk.cart.controller.grpc;

import com.gdn.project.waroenk.cart.*;
import com.gdn.project.waroenk.cart.entity.Cart;
import com.gdn.project.waroenk.cart.entity.CartItem;
import com.gdn.project.waroenk.cart.mapper.CartMapper;
import com.gdn.project.waroenk.cart.service.CartService;
import com.gdn.project.waroenk.common.Basic;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
public class CartController extends CartServiceGrpc.CartServiceImplBase {

    private static final CartMapper mapper = CartMapper.INSTANCE;
    private final CartService cartService;

    @Override
    public void getCart(GetCartRequest request, StreamObserver<CartData> responseObserver) {
        Cart cart = cartService.getCart(request.getUserId());
        CartData response = mapper.toResponseGrpc(cart);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void addItem(AddCartItemRequest request, StreamObserver<CartData> responseObserver) {
        CartItem item = mapper.toCartItemEntity(request);
        Cart cart = cartService.addItem(request.getUserId(), item);
        CartData response = mapper.toResponseGrpc(cart);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void bulkAddItems(BulkAddCartItemsRequest request, StreamObserver<CartData> responseObserver) {
        List<CartItem> items = request.getItemsList().stream()
                .map(mapper::toCartItemEntity)
                .collect(Collectors.toList());
        Cart cart = cartService.bulkAddItems(request.getUserId(), items);
        CartData response = mapper.toResponseGrpc(cart);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void removeItem(RemoveCartItemRequest request, StreamObserver<CartData> responseObserver) {
        Cart cart = cartService.removeItem(request.getUserId(), request.getSku());
        CartData response = mapper.toResponseGrpc(cart);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void bulkRemoveItems(BulkRemoveCartItemsRequest request, StreamObserver<CartData> responseObserver) {
        Cart cart = cartService.bulkRemoveItems(request.getUserId(), request.getSkusList());
        CartData response = mapper.toResponseGrpc(cart);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateItem(UpdateCartItemRequest request, StreamObserver<CartData> responseObserver) {
        Cart cart = cartService.updateItemQuantity(request.getUserId(), request.getSku(), request.getQuantity());
        CartData response = mapper.toResponseGrpc(cart);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void clearCart(ClearCartRequest request, StreamObserver<Basic> responseObserver) {
        boolean result = cartService.clearCart(request.getUserId());
        Basic response = Basic.newBuilder().setStatus(result).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void filterCarts(FilterCartRequest request, StreamObserver<MultipleCartResponse> responseObserver) {
        MultipleCartResponse response = cartService.filterCarts(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}




