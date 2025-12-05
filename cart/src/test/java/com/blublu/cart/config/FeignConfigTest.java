// src/test/java/com/blublu/cart/config/FeignConfigTest.java
package com.blublu.cart.config;

import com.blublu.cart.exception.ProductNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeignConfigTest {

  @Mock
  private Response response;

  @Mock
  private Response.Body responseBody;

  @Mock
  private ErrorDecoder errorDecoder;

  private final FeignConfig.ProductNotFoundFeignErrorDecoder decoder =
      new FeignConfig.ProductNotFoundFeignErrorDecoder();

  @Test
  void decode_withErrorMessageInBody_returnsExceptionWithThatMessage() throws Exception {
    String json = "{\"errorMessage\":\"Custom product missing\"}";

    when(response.body()).thenReturn(responseBody);
    when(responseBody.asReader(StandardCharsets.UTF_8)).thenReturn(new StringReader(json));

    Exception ex = decoder.decode("methodKey", response);

    assertNotNull(ex);
    assertTrue(ex instanceof ProductNotFoundException);
    assertEquals("Custom product missing", ex.getMessage());
  }

  @Test
  void decode_withoutErrorMessage_returnsDefaultProductNotFoundMessage() throws Exception {
    String json = "{\"some\":\"value\"}";

    when(response.body()).thenReturn(responseBody);
    when(responseBody.asReader(StandardCharsets.UTF_8)).thenReturn(new StringReader(json));

    Exception ex = decoder.decode("methodKey", response);

    assertNotNull(ex);
    assertInstanceOf(ProductNotFoundException.class, ex);
    assertEquals("Product not found!", ex.getMessage());
  }

  @Test
  void decode_withInvalidJson_returnsUnknownErrorOccuredMessage() throws Exception {
    String invalid = "not a json";

    when(response.body()).thenReturn(responseBody);
    when(responseBody.asReader(StandardCharsets.UTF_8)).thenReturn(new StringReader(invalid));

    Exception ex = decoder.decode("methodKey", response);

    assertNotNull(ex);
    assertInstanceOf(ProductNotFoundException.class, ex);
    assertEquals("Unknown error occured.", ex.getMessage());
  }

  @Test
  void errorDecoderBean_returnsProductNotFoundFeignErrorDecoder() {
    FeignConfig config = new FeignConfig();
    ErrorDecoder decoder = config.errorDecoder();

    assertNotNull(decoder);
    assertInstanceOf(FeignConfig.ProductNotFoundFeignErrorDecoder.class, decoder);
  }
}
