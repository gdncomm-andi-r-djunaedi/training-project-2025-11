package com.example.cart.utils;

import com.example.cart.dto.CartDTO;
import com.example.cart.dto.ProductDTO;
import com.example.cart.dto.response.ProductServiceResponse;
import com.example.cart.entity.Cart;
import com.example.cart.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class DTOUtils {

    // Do not allow to create an Object as all the methods are static
    private DTOUtils() {

    }

    public static ProductDTO getDTO(Product srcProduct) {
        ProductDTO targetDTO = new ProductDTO();
        BeanUtils.copyProperties(srcProduct, targetDTO);
        log.debug("getDTO():: srcProduct - {}, targetDTO - {}", srcProduct, targetDTO);
        return targetDTO;
    }

    public static Product getEntity(ProductServiceResponse productServiceResponse) {
        Product targetEntity = new Product();
        BeanUtils.copyProperties(productServiceResponse, targetEntity);
        log.debug("getEntity():: productServiceResponse - {}, targetEntity - {}", productServiceResponse, targetEntity);
        return targetEntity;
    }

    public static CartDTO getDTO(Cart srcCart) {
        CartDTO targetDTO = new CartDTO();
        targetDTO.setId(srcCart.getId());
        targetDTO.setTotalPrice(srcCart.getTotalPrice());
        if (srcCart.getCartItems() != null) {
            List<ProductDTO> productDTOs = srcCart.getCartItems().stream()
                    .map(DTOUtils::getDTO)
                    .toList();
            targetDTO.setCartItems(productDTOs);
        } else {
            targetDTO.setCartItems(new ArrayList<>());
        }
        log.debug("getDTO():: srcCart - {}, targetDTO - {}", srcCart, targetDTO);
        return targetDTO;
    }
}
