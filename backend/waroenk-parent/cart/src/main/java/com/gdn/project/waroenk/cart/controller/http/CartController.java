package com.gdn.project.waroenk.cart.controller.http;

import com.gdn.project.waroenk.cart.*;
import com.gdn.project.waroenk.cart.constant.ApiConstant;
import com.gdn.project.waroenk.cart.dto.BasicDto;
import com.gdn.project.waroenk.cart.dto.cart.*;
import com.gdn.project.waroenk.cart.mapper.CartMapper;
import com.gdn.project.waroenk.common.Basic;
import com.gdn.project.waroenk.common.SortBy;
import jakarta.validation.Valid;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping(ApiConstant.BASE_PATH)
@RestController("cartHttpController")
public class CartController {

    private static final CartMapper mapper = CartMapper.INSTANCE;
    private final CartServiceGrpc.CartServiceBlockingStub grpcClient;

    @Autowired
    public CartController(
            @GrpcClient("cart-service") CartServiceGrpc.CartServiceBlockingStub grpcClient) {
        this.grpcClient = grpcClient;
    }

    @GetMapping("/cart/{userId}")
    public CartResponseDto getCart(@PathVariable String userId) {
        CartData response = grpcClient.getCart(GetCartRequest.newBuilder().setUserId(userId).build());
        return mapper.toResponseDto(response);
    }

    @PostMapping("/cart/add")
    public CartResponseDto addItem(@Valid @RequestBody AddCartItemRequestDto requestDto) {
        CartData response = grpcClient.addItem(mapper.toAddItemRequestGrpc(requestDto));
        return mapper.toResponseDto(response);
    }

    @PostMapping("/cart/bulk-add")
    public CartResponseDto bulkAddItems(@Valid @RequestBody BulkAddCartItemsRequestDto requestDto) {
        CartData response = grpcClient.bulkAddItems(mapper.toBulkAddRequestGrpc(requestDto));
        return mapper.toResponseDto(response);
    }

    @PostMapping("/cart/remove")
    public CartResponseDto removeItem(@Valid @RequestBody RemoveCartItemRequestDto requestDto) {
        CartData response = grpcClient.removeItem(mapper.toRemoveItemRequestGrpc(requestDto));
        return mapper.toResponseDto(response);
    }

    @PostMapping("/cart/bulk-remove")
    public CartResponseDto bulkRemoveItems(@Valid @RequestBody BulkRemoveCartItemsRequestDto requestDto) {
        CartData response = grpcClient.bulkRemoveItems(mapper.toBulkRemoveRequestGrpc(requestDto));
        return mapper.toResponseDto(response);
    }

    @PutMapping("/cart/update")
    public CartResponseDto updateItem(@Valid @RequestBody UpdateCartItemRequestDto requestDto) {
        CartData response = grpcClient.updateItem(mapper.toUpdateItemRequestGrpc(requestDto));
        return mapper.toResponseDto(response);
    }

    @DeleteMapping("/cart/{userId}")
    public BasicDto clearCart(@PathVariable String userId) {
        Basic response = grpcClient.clearCart(ClearCartRequest.newBuilder().setUserId(userId).build());
        return mapper.toBasicDto(response);
    }

    @GetMapping("/cart/filter")
    public ListOfCartResponseDto filterCarts(
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
        FilterCartRequest.Builder builder = FilterCartRequest.newBuilder().setSize(size);
        if (StringUtils.isNotBlank(userId)) builder.setUserId(userId);
        if (StringUtils.isNotBlank(cursor)) builder.setCursor(cursor);
        builder.setSortBy(SortBy.newBuilder().setField(sortBy).setDirection(sortDirection).build());

        MultipleCartResponse response = grpcClient.filterCarts(builder.build());
        return mapper.toResponseDto(response);
    }
}




