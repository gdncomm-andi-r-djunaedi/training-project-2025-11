package com.kailash.cart;

import com.kailash.cart.repository.CartRepository;
import com.kailash.cart.service.impl.CartServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import com.kailash.cart.client.ProductClient;
import com.kailash.cart.dto.ApiResponse;
import com.kailash.cart.dto.CartResponse;
import com.kailash.cart.dto.ProductResponse;
import com.kailash.cart.entity.Cart;
import com.kailash.cart.entity.CartItem;
import com.kailash.cart.exception.NotFoundException;
import com.kailash.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@SpringBootTest
class CartApplicationTests {

	@Test
	void contextLoads() {
	}

	@Mock
	private CartRepository cartRepository;

	@Mock
	private ProductClient productClient;

	@InjectMocks
	private CartServiceImpl cartService;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
	}

	private ProductResponse createMockProduct(String sku, String name, double price) {
		return new ProductResponse(sku, name, "Short description", price);
	}

	// -------------------------------------------------------------------------
	// getCart
	// -------------------------------------------------------------------------
	@Test
	void getCart_existingCart_returnsCart() {
		Cart cart = new Cart();
		cart.setId("cart-1");
		cart.setMemberId("member-1");
		cart.setItems(new ArrayList<>());
		cart.setTotalItems(0);
		cart.setTotalPrice(0.0);

		when(cartRepository.findByMemberId("member-1")).thenReturn(Optional.of(cart));

		ApiResponse<CartResponse> resp = cartService.getCart("member-1");

		assertTrue(resp.isSuccess());
		assertEquals("member-1", resp.getData().getMemberId());
	}

	@Test
	void getCart_noCart_createsNewCart() {
		when(cartRepository.findByMemberId("member-1")).thenReturn(Optional.empty());

		Cart savedCart = new Cart();
		savedCart.setId("cart-1");
		savedCart.setMemberId("member-1");
		savedCart.setItems(new ArrayList<>());
		savedCart.setTotalItems(0);
		savedCart.setTotalPrice(0.0);

		when(cartRepository.save(any())).thenReturn(savedCart);

		ApiResponse<CartResponse> resp = cartService.getCart("member-1");

		assertTrue(resp.isSuccess());
		assertEquals("member-1", resp.getData().getMemberId());
	}

	// -------------------------------------------------------------------------
	// addOrUpdateItem
	// -------------------------------------------------------------------------
	@Test
	void addOrUpdateItem_addsNewItem() {
		Cart emptyCart = new Cart();
		emptyCart.setId("cart-1");
		emptyCart.setMemberId("member-1");
		emptyCart.setItems(new ArrayList<>());

		when(cartRepository.findByMemberId("member-1")).thenReturn(Optional.of(emptyCart));
		when(productClient.get("SKU-1")).thenReturn(
				ResponseEntity.ok(new ApiResponse<>(true, "ok", createMockProduct("SKU-1", "Product A", 100.0)))
		);
		when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		ApiResponse<CartResponse> resp = cartService.addOrUpdateItem("member-1", "SKU-1", 2);

		assertTrue(resp.isSuccess());
		assertEquals(2, resp.getData().getTotalItems());
		assertEquals(200.0, resp.getData().getTotalPrice());
	}

	@Test
	void addOrUpdateItem_updatesExistingItem() {
		CartItem item = new CartItem();
		item.setSku("SKU-1");
		item.setQty(1);
		item.setPriceSnapshot(100.0);
		item.setProductName("Product A");

		Cart cart = new Cart();
		cart.setId("cart-1");
		cart.setMemberId("member-1");
		cart.setItems(new ArrayList<>());
		cart.getItems().add(item);

		when(cartRepository.findByMemberId("member-1")).thenReturn(Optional.of(cart));
		when(productClient.get("SKU-1")).thenReturn(
				ResponseEntity.ok(new ApiResponse<>(true, "ok", createMockProduct("SKU-1", "Product A", 100.0)))
		);
		when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		ApiResponse<CartResponse> resp = cartService.addOrUpdateItem("member-1", "SKU-1", 3);

		assertTrue(resp.isSuccess());
		assertEquals(4, resp.getData().getTotalItems()); // 1 + 3
		assertEquals(400.0, resp.getData().getTotalPrice());
	}

	@Test
	void addOrUpdateItem_qtyZero_returnsError() {
		ApiResponse<CartResponse> resp = cartService.addOrUpdateItem("member-1", "SKU-1", 0);
		assertFalse(resp.isSuccess());
	}

	// -------------------------------------------------------------------------
	// removeItem
	// -------------------------------------------------------------------------
	@Test
	void removeItem_removesExistingItem() {
		CartItem item = new CartItem();
		item.setSku("SKU-1");
		item.setQty(2);
		item.setPriceSnapshot(100.0);
		item.setProductName("Product A");

		Cart cart = new Cart();
		cart.setId("cart-1");
		cart.setMemberId("member-1");
		cart.setItems(new ArrayList<>());
		cart.getItems().add(item);
		cart.setTotalItems(2);
		cart.setTotalPrice(200.0);

		when(cartRepository.findByMemberId("member-1")).thenReturn(Optional.of(cart));
		when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		ApiResponse<CartResponse> resp = cartService.removeItem("member-1", "SKU-1");

		assertTrue(resp.isSuccess());
		assertEquals(0, resp.getData().getTotalItems());
		assertEquals(0.0, resp.getData().getTotalPrice());
	}

	@Test
	void removeItem_cartEmpty_returnsError() {
		when(cartRepository.findByMemberId("member-1")).thenReturn(Optional.empty());

		ApiResponse<?> resp = cartService.removeItem("member-1", "SKU-1");

		assertFalse(resp.isSuccess());
		assertEquals("Cart is empty for the given member", resp.getMessage());
	}

}
