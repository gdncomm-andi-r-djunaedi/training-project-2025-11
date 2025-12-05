package com.blibli.training.framework.dto;

import com.blibli.training.framework.constant.SortDirection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BasePagingRequestTest {

    @Test
    public void testDefaultValues() {
        BasePagingRequest request = new BasePagingRequest();

        assertEquals(0, request.getPage());
        assertEquals(10, request.getSize());
        assertEquals(SortDirection.ASC, request.getSortDirection());
        assertNull(request.getSortBy());
        assertNull(request.getSearch());
    }

    @Test
    public void testBuilderValues() {
        BasePagingRequest request = BasePagingRequest.builder()
                .page(1)
                .size(20)
                .sortBy("name")
                .sortDirection(SortDirection.DESC)
                .search("query")
                .build();

        assertEquals(1, request.getPage());
        assertEquals(20, request.getSize());
        assertEquals("name", request.getSortBy());
        assertEquals(SortDirection.DESC, request.getSortDirection());
        assertEquals("query", request.getSearch());
    }

    @Test
    public void testSetters() {
        BasePagingRequest request = new BasePagingRequest();
        request.setPage(2);
        request.setSize(50);
        request.setSortBy("createdAt");
        request.setSortDirection(SortDirection.DESC);
        request.setSearch("test");

        assertEquals(2, request.getPage());
        assertEquals(50, request.getSize());
        assertEquals("createdAt", request.getSortBy());
        assertEquals(SortDirection.DESC, request.getSortDirection());
        assertEquals("test", request.getSearch());
    }
}
