package com.blublu.cart.interfaces;

import com.blublu.cart.config.FeignConfig;
import com.blublu.cart.model.response.CartResponse;
import com.blublu.cart.model.response.GenericBodyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Component
@FeignClient(name = "product", url = "localhost:8080/", path = "product", configuration = FeignConfig.class)
public interface ProductFeignClient {
  @RequestMapping(value = "/{skuCode}/_detail", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  GenericBodyResponse<CartResponse.ItemResponse> getProductDetail(@PathVariable String skuCode);

}
