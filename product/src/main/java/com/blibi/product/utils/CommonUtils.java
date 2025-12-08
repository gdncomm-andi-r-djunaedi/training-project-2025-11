package com.blibi.product.utils;

import com.blibi.product.dto.ProductDTO;
import com.blibi.product.entity.Product;
import org.springframework.beans.BeanUtils;

public class CommonUtils {
    
   // Converts a Product entity to a ProductDTO.

    public static ProductDTO getProductDTO(Product product) {

        ProductDTO targetDTO = new ProductDTO();
        BeanUtils.copyProperties(product, targetDTO);
        return targetDTO;
    }
    
    // Converts a ProductDTO to a Product entity.

    public static Product getProductFromDTO(ProductDTO productDTO) {

        Product target = new Product();
        BeanUtils.copyProperties(productDTO, target);
        return target;
    }
}
