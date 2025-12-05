package com.blublu.product.model.response;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GenericBodyResponseTest {

    @Test
    void testGenericBodyResponseLombok() {
        List<String> content = Collections.singletonList("test");
        GenericBodyResponse response1 = GenericBodyResponse.builder()
                .errorMessage("Error")
                .errorCode(500)
                .success(false)
                .content(content)
                .build();

        GenericBodyResponse response2 = GenericBodyResponse.builder()
            .errorCode(500)
            .errorMessage("Error")
            .success(false)
            .content(content)
            .build();

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertEquals(response1.toString(), response2.toString());

        assertEquals("Error", response1.getErrorMessage());
        assertEquals(500, response1.getErrorCode());
        assertEquals(false, response1.isSuccess());
        assertEquals(content, response1.getContent());
    }
}
