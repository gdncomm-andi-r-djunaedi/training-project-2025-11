package com.blublu.product.model.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class GetProductDetailRequestTest {

    @Test
    void testGetProductDetailRequestLombok() {
        GetProductDetailRequest request1 = new GetProductDetailRequest();
        request1.setProductId(123L);

        GetProductDetailRequest request2 = new GetProductDetailRequest();
        request2.setProductId(123L);

        GetProductDetailRequest request3 = new GetProductDetailRequest();
        request3.setProductId(456L);

        assertEquals(123L, request1.getProductId());
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1, request3);

        String toString = request1.toString();
        assertEquals("GetProductDetailRequest(productId=123)", toString);
    }
}
