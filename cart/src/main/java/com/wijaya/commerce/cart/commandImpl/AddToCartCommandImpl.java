package com.wijaya.commerce.cart.commandImpl;

import org.springframework.stereotype.Service;

import com.wijaya.commerce.cart.command.AddToCartCommand;
import com.wijaya.commerce.cart.commandImpl.model.AddToCartCommandRequest;
import com.wijaya.commerce.cart.commandImpl.model.AddToCartCommandResponse;
import com.wijaya.commerce.cart.outbond.outbondModel.response.GetDetailProductOutbondResponse;
import com.wijaya.commerce.cart.outbond.outbondModel.response.GetDetailUserOutbondResponse;
import com.wijaya.commerce.cart.outbond.outbondService.ProductOutbondService;
import com.wijaya.commerce.cart.outbond.outbondService.UserOutbondService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddToCartCommandImpl implements AddToCartCommand {

    private final UserOutbondService userOutbondService;

    private final ProductOutbondService productOutbondService;

    @Override
    public AddToCartCommandResponse doCommand(AddToCartCommandRequest request) {
        if (!isUserExistAndActive(request.getUserId())) {
            throw new RuntimeException("User not found or not active");
        }
        throw new UnsupportedOperationException("Unimplemented method 'doCommand'");
    }

    private boolean isUserExistAndActive(String userId) {
        GetDetailUserOutbondResponse user = userOutbondService.getUserDetail(userId);
        return user != null && "ACTIVE".equals(user.getStatus());
    }

    private boolean isProductExistAndActive(String sku) {
        GetDetailProductOutbondResponse product = productOutbondService.getProductDetail(sku);
        return product != null && "ACTIVE".equals(product.getStatus());
    }

}
