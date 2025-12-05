package com.gdn.project.waroenk.cart.controller.grpc;

import com.gdn.project.waroenk.cart.*;
import com.gdn.project.waroenk.cart.dto.checkout.FinalizeCheckoutResponseDto;
import com.gdn.project.waroenk.cart.dto.checkout.ValidateCheckoutResponseDto;
import com.gdn.project.waroenk.cart.entity.Checkout;
import com.gdn.project.waroenk.cart.mapper.CheckoutMapper;
import com.gdn.project.waroenk.cart.service.CheckoutService;
import com.gdn.project.waroenk.common.Basic;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class CheckoutController extends CheckoutServiceGrpc.CheckoutServiceImplBase {

    private static final CheckoutMapper mapper = CheckoutMapper.INSTANCE;
    private final CheckoutService checkoutService;

    @Override
    public void validateAndReserve(ValidateCheckoutRequest request, StreamObserver<ValidateCheckoutResponse> responseObserver) {
        ValidateCheckoutResponseDto result = checkoutService.validateAndReserve(request.getUserId());
        ValidateCheckoutResponse response = mapper.toValidateResponseGrpc(result);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void invalidateCheckout(InvalidateCheckoutRequest request, StreamObserver<Basic> responseObserver) {
        boolean result = checkoutService.invalidateCheckout(request.getCheckoutId());
        Basic response = Basic.newBuilder().setStatus(result).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getCheckout(GetCheckoutRequest request, StreamObserver<CheckoutData> responseObserver) {
        Checkout checkout = checkoutService.getCheckout(request.getCheckoutId());
        CheckoutData response = mapper.toResponseGrpc(checkout);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getCheckoutByUser(GetCheckoutByUserRequest request, StreamObserver<CheckoutData> responseObserver) {
        Checkout checkout = checkoutService.getCheckoutByUser(request.getUserId());
        CheckoutData response = mapper.toResponseGrpc(checkout);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void finalizeCheckout(FinalizeCheckoutRequest request, StreamObserver<FinalizeCheckoutResponse> responseObserver) {
        FinalizeCheckoutResponseDto result = checkoutService.finalizeCheckout(request.getCheckoutId(), request.getOrderId());
        FinalizeCheckoutResponse response = mapper.toFinalizeResponseGrpc(result);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}




