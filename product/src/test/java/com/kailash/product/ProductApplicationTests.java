package com.kailash.product;

import com.kailash.product.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.kailash.product.entity.Product;
import com.kailash.product.repository.ProductRepository;
import com.kailash.product.service.ProductEventProducer;
import com.kailash.product.service.ProductIndexService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ProductApplicationTests {

	@Test
	void contextLoads() {
	}
	@Mock
	ProductRepository repo;

	@Mock
	ProductIndexService productIndexService;

	@Mock
	ProductEventProducer productEventProducer;

	@InjectMocks
	ProductServiceImpl service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}


	@Test
	void testCreateSuccess() {
		Product p = new Product();
		p.setSku("A1");

		when(repo.findBySku("A1")).thenReturn(Optional.empty());

		Product saved = new Product();
		saved.setSku("A1");

		when(repo.save(p)).thenReturn(saved);

		Product result = service.create(p);

		assertEquals("A1", result.getSku());
		verify(repo).save(p);
		verify(productEventProducer).sendProductUpsert(saved);
	}

	@Test
	void testCreateThrowsExceptionWhenSkuExists() {
		Product p = new Product();
		p.setSku("A1");

		when(repo.findBySku("A1")).thenReturn(Optional.of(new Product()));

		assertThrows(IllegalArgumentException.class,
				() -> service.create(p));
	}


	@Test
	void testFindBySku() {
		Product p = new Product();
		p.setSku("A1");

		when(repo.findBySku("A1")).thenReturn(Optional.of(p));

		Optional<Product> result = service.findBySku("A1");
		assertTrue(result.isPresent());
		assertEquals("A1", result.get().getSku());
	}


	@Test
	void testList_NoSearch() {
		Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
		Page<Product> page = new PageImpl<>(List.of(new Product()));

		when(repo.findAll(pageable)).thenReturn(page);

		Page<Product> result = service.list("", 0, 10);
		assertEquals(1, result.getTotalElements());
	}

	@Test
	void testList_WithSearch() {
		Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
		Page<Product> page = new PageImpl<>(List.of(new Product()));

		when(repo.findByNameContainingIgnoreCase("abc", pageable)).thenReturn(page);

		Page<Product> result = service.list("abc", 0, 10);
		assertEquals(1, result.getTotalElements());
	}


	@Test
	void testUpdateSuccess() {
		Product existing = new Product();
		existing.setSku("A1");
		existing.setName("Old");

		Product updated = new Product();
		updated.setName("New");

		when(repo.findBySku("A1")).thenReturn(Optional.of(existing));
		when(repo.save(existing)).thenReturn(existing);

		Product result = service.update("A1", updated);

		assertEquals("New", result.getName());
		verify(productEventProducer).sendProductUpsert(existing);
	}

	@Test
	void testUpdateNotFound() {
		when(repo.findBySku("A1")).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class,
				() -> service.update("A1", new Product()));
	}


	@Test
	void testDeleteSuccess() {
		Product existing = new Product();
		existing.setId("10L");
		existing.setSku("A1");

		when(repo.findBySku("A1")).thenReturn(Optional.of(existing));

		service.delete("A1");

		verify(repo).deleteById("10L");
		verify(productEventProducer).sendProductDelete("10L");
	}

	@Test
	void testDeleteNoOpWhenSkuNotFound() {
		when(repo.findBySku("A1")).thenReturn(Optional.empty());

		service.delete("A1");

		verify(repo, never()).deleteById(any());
		verify(productEventProducer, never()).sendProductDelete(any());
	}

}
