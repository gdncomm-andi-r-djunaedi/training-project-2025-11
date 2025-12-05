package com.gdn.project.waroenk.cart.controller.http;

import com.gdn.project.waroenk.cart.*;
import com.gdn.project.waroenk.cart.constant.ApiConstant;
import com.gdn.project.waroenk.cart.dto.BasicDto;
import com.gdn.project.waroenk.cart.dto.checkout.*;
import com.gdn.project.waroenk.cart.mapper.CheckoutMapper;
import com.gdn.project.waroenk.common.Basic;
import jakarta.validation.Valid;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping(ApiConstant.BASE_PATH)
@RestController("checkoutHttpController")
public class CheckoutController {

    private static final CheckoutMapper mapper = CheckoutMapper.INSTANCE;
    private final CheckoutServiceGrpc.CheckoutServiceBlockingStub grpcClient;

    @Autowired
    public CheckoutController(
            @GrpcClient("cart-service") CheckoutServiceGrpc.CheckoutServiceBlockingStub grpcClient) {
        this.grpcClient = grpcClient;
    }

    @PostMapping("/checkout/validate")
    public ValidateCheckoutResponseDto validateAndReserve(@Valid @RequestBody ValidateCheckoutRequestDto requestDto) {
        ValidateCheckoutResponse response = grpcClient.validateAndReserve(mapper.toValidateRequestGrpc(requestDto));
        return mapper.toValidateResponseDto(response);
    }

    @PostMapping("/checkout/invalidate")
    public BasicDto invalidateCheckout(@Valid @RequestBody InvalidateCheckoutRequestDto requestDto) {
        Basic response = grpcClient.invalidateCheckout(mapper.toInvalidateRequestGrpc(requestDto));
        return mapper.toBasicDto(response);
    }

    @GetMapping("/checkout/{checkoutId}")
    public CheckoutResponseDto getCheckout(@PathVariable String checkoutId) {
        CheckoutData response = grpcClient.getCheckout(
                GetCheckoutRequest.newBuilder().setCheckoutId(checkoutId).build());
        return mapper.toResponseDto(response);
    }

    @GetMapping("/checkout/user/{userId}")
    public CheckoutResponseDto getCheckoutByUser(@PathVariable String userId) {
        CheckoutData response = grpcClient.getCheckoutByUser(
                GetCheckoutByUserRequest.newBuilder().setUserId(userId).build());
        return mapper.toResponseDto(response);
    }

    @PostMapping("/checkout/finalize")
    public FinalizeCheckoutResponseDto finalizeCheckout(@Valid @RequestBody FinalizeCheckoutRequestDto requestDto) {
        FinalizeCheckoutResponse response = grpcClient.finalizeCheckout(mapper.toFinalizeRequestGrpc(requestDto));
        return mapper.toFinalizeResponseDto(response);
    }
}




