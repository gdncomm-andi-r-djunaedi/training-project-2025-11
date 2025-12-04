package com.kailash.search;

import com.kailash.search.service.impl.SearchServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import com.kailash.search.dto.ProductPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.*;

import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class SearchApplicationTests {

	@Test
	void contextLoads() {
	}

	@Mock
	private ElasticsearchOperations esOps;

	@InjectMocks
	private SearchServiceImpl searchService;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);

		// Set indexName field manually since @Value does not inject in tests
		// indexer.es.index-name defaults to "products"
		try {
			var field = SearchServiceImpl.class.getDeclaredField("indexName");
			field.setAccessible(true);
			field.set(searchService, "products");
		} catch (Exception ignored) {}
	}

//	@Test
//	void testGetById() {
//		String id = "123";
//
//		ProductPayload payload = new ProductPayload();
//		payload.setId(id);
//		payload.setName("Test Product");
//
//		// THIS WILL NEVER FAIL — MATCHES ANY PARAMETERS
//		when(esOps.get(
//				any(),
//				any(),
//				any()
//		)).thenReturn(payload);
//
//		ProductPayload result = searchService.getById(id);
//
//		assertNotNull(result);
//		assertEquals("123", result.getId());
//		assertEquals("Test Product", result.getName());
//	}



//	@Test
//	void testSearchByName() {
//		// Arrange
//		String searchText = "phone";
//
//		ProductPayload p1 = new ProductPayload();
//		p1.setId("1");
//		p1.setName("iPhone 15");
//
//		ProductPayload p2 = new ProductPayload();
//		p2.setId("2");
//		p2.setName("Samsung Phone");
//
//		SearchHit<ProductPayload> hit1 = mock(SearchHit.class);
//		when(hit1.getContent()).thenReturn(p1);
//
//		SearchHit<ProductPayload> hit2 = mock(SearchHit.class);
//		when(hit2.getContent()).thenReturn(p2);
//
//		SearchHits<ProductPayload> mockHits = mock(SearchHits.class);
//		when(mockHits.getSearchHits()).thenReturn(List.of(hit1, hit2));
//
//		// Match ANY NativeQuery object and the expected class + index
//		when(esOps.search(
//				any(NativeQuery.class),
//				eq(ProductPayload.class),
//				eq(IndexCoordinates.of("products"))
//		)).thenReturn(mockHits);
//
//		// Act
//		List<ProductPayload> results = searchService.searchByName(searchText);
//
//		// Assert
//		assertEquals(2, results.size());
//		assertEquals("iPhone 15", results.get(0).getName());
//		assertEquals("Samsung Phone", results.get(1).getName());
//	}

}
