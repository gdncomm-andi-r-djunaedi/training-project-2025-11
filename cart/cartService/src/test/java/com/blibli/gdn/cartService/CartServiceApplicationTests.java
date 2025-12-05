package com.blibli.gdn.cartService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
	"spring.data.mongodb.uri=mongodb://localhost:27017/test",
	"product.service.url=http://localhost:8082"
})
class CartServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
