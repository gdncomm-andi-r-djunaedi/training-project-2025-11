package com.blublu.cart.config;

import com.blublu.cart.exception.ProductNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class FeignConfig{

  @Bean
  public ErrorDecoder errorDecoder() {
    return new ProductNotFoundFeignErrorDecoder();
  }

  public static class ProductNotFoundFeignErrorDecoder implements ErrorDecoder {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
      try {
        String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));

        JsonNode jsonNode = objectMapper.readTree(body);

        String errorMessage = jsonNode.path("errorMessage").asText("Product not found!");

        // Return custom exception with clean message
        return new ProductNotFoundException(errorMessage);

      } catch (IOException e) {
        return new ProductNotFoundException("Unknown error occured.");
      }
    }
  }
}
