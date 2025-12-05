package com.ecom.cart.Service.Impl;

import com.ecom.cart.Dto.ApiResponse;
import com.ecom.cart.Dto.CartDto;
import com.ecom.cart.Dto.CartItemDto;
import com.ecom.cart.Dto.ProductDto;
import com.ecom.cart.Entity.Cart;
import com.ecom.cart.Entity.CartItem;
import com.ecom.cart.Repository.CartRepo;
import com.ecom.cart.Service.CartService;
import com.ecom.cart.client.ProductClient;
import com.ecom.cart.exception.NoDataFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartRepo cartRepo;

    @Autowired
    ProductClient productClient;

    @Override
    public CartDto getCartByUserId(String userId) {
        Cart cart = cartRepo.findByUserId(userId).orElseThrow(() -> new NoDataFoundException("Empty Cart found for user: " + userId));

        List<CartItemDto> items = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            CartItemDto dto = new CartItemDto();
            BeanUtils.copyProperties(item,dto);
            items.add(dto);
        }

        CartDto cartDto = new CartDto();
//        cartDto.setUserId(cart.getUserId());
        BeanUtils.copyProperties(cart,cartDto);
        cartDto.setItems(items);


        return cartDto;
    }

    @Override
    public Boolean deleteFromCartBySku(String sku, String userId) {
        Cart cart = cartRepo.findByUserId(userId).orElseThrow(() -> new NoDataFoundException("Cart not found for user: " + userId));

        List<CartItem> items = cart.getItems();

        for (CartItem ci: items) {
            if (ci.getProductSku().equals(sku)) {
                items.remove(ci);
                break;
            }
        }

        cartRepo.save(cart);
        return true;

    }

    @Override
    public Boolean addSkuToCart(String sku, String userId) {
        Cart cart = cartRepo.findByUserId(userId).orElse(null);

        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setItems(new ArrayList<>());
        }

        List<CartItem> items = cart.getItems();
        boolean exists = false;

        for (CartItem ci : items) {
            if (ci.getProductSku().equals(sku)) {
                ci.setQuantity(ci.getQuantity() + 1);
                double newPrice = (ci.getPrice()/(ci.getQuantity()-1))*ci.getQuantity();
                ci.setPrice(Math.round(newPrice * 1000.0) / 1000.0);
                exists = true;
                break;
            }
        }

        if (!exists) {
            ApiResponse<ProductDto> product = productClient.getProductBySku(sku);

            if (product == null) {
                throw new NoDataFoundException("Product not found with SKU: " + sku);
            }

            CartItem newItem = new CartItem();
            BeanUtils.copyProperties(product.getData(),newItem);
            newItem.setQuantity(1);

            items.add(newItem);
        }

        cartRepo.save(cart);

        return true;
    }
}

