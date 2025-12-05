package com.gdn.faurihakim.cart.command.impl;

import com.gdn.faurihakim.Cart;
import com.gdn.faurihakim.CartRepository;
import com.gdn.faurihakim.cart.command.DeleteCartCommand;
import com.gdn.faurihakim.cart.command.model.DeleteCartCommandRequest;
import com.gdn.faurihakim.cart.web.model.CartNotFoundException;
import com.gdn.faurihakim.cart.web.model.response.DeleteCartWebResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class DeleteCartCommandImpl implements DeleteCartCommand {
    @Autowired
    private CartRepository cartRepository;

    @Override
    @Transactional
    public DeleteCartWebResponse execute(DeleteCartCommandRequest request) {
        Cart cart = cartRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new CartNotFoundException("Cart not found for member: " + request.getMemberId()));

        // Delete cart (cascade deletes all cart items)
        cartRepository.delete(cart);

        return DeleteCartWebResponse.builder()
                .message("Cart deleted successfully")
                .memberId(request.getMemberId())
                .build();
    }
}
