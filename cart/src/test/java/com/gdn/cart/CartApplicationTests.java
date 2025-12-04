package com.gdn.cart;

import com.gdn.cart.client.ProductClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class CartApplicationTests {

	@MockBean
	private ProductClient productClient;

	@Test
	void contextLoads() {
	}

}
