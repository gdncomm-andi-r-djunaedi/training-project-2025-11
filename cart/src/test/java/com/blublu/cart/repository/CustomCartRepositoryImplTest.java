package com.blublu.cart.repository;

import com.blublu.cart.document.CartDocument;
import com.blublu.cart.model.request.EditQtyRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomCartRepositoryImplTest {
  @Mock
  MongoTemplate mongoTemplate;

  @InjectMocks
  CustomCartRepositoryImpl customCartRepository;

  @Test
  public void addOrUpdateItemTest() {
    String username = "test";
    int qty = 5;
    String skuCode = "MTA-TEST";
    Query checkQuery = new Query(Criteria.where("username").is(username).and("items.skuCode").is(skuCode));
    Update update = new Update().inc("items.$.quantity", qty);
    CartDocument.Item item = new CartDocument.Item();
    item.setQuantity(qty);
    item.setSkuCode(skuCode);

    // Test item exist
    when(mongoTemplate.exists(checkQuery, CartDocument.class)).thenReturn(true);
    assertTrue(customCartRepository.addOrUpdateItem(username, item));
    verify(mongoTemplate).updateFirst(checkQuery, update, CartDocument.class);

    // Test item not exist
    Query userQuery = new Query(Criteria.where("username").is(username));

    when(mongoTemplate.exists(checkQuery, CartDocument.class)).thenReturn(false);
    assertTrue(customCartRepository.addOrUpdateItem(username, item));

  }

  @Test
  public void editCartItemTest() {
    String username = "username";
    String skuCode = "MTA-TEST";
    int qty = 5;

    Query query = new Query(Criteria.where("username").is(username).and("items.skuCode").is(skuCode));
    Update update = new Update().set("items.$.quantity", qty);

    EditQtyRequest editQtyRequest = EditQtyRequest.builder().skuCode(skuCode).newQty(qty).build();

    // Check item exist
    when(mongoTemplate.exists(query, CartDocument.class)).thenReturn(true);
    assertTrue(customCartRepository.editCartItem(username, editQtyRequest));
    verify(mongoTemplate).updateFirst(query, update, CartDocument.class);

    // Check item not exist
    when(mongoTemplate.exists(query, CartDocument.class)).thenReturn(false);
    assertFalse(customCartRepository.editCartItem(username, editQtyRequest));
  }

  @Test
  public void removeItemFromCartUnitNegativeTest() {
    String username = "username";
    String skuCode = "MTA-TEST";
    int qty = 5;

    // Check item not found in cart
    when(mongoTemplate.findOne(any(), eq(CartDocument.class))).thenReturn(null);
    customCartRepository.removeItemFromCart(username, skuCode);
    assertFalse(customCartRepository.removeItemFromCart(username, skuCode));

    reset(mongoTemplate);
    CartDocument itemNull = CartDocument.builder().id("1").username(username).items(null).build();

    when(mongoTemplate.findOne(any(), eq(CartDocument.class))).thenReturn(itemNull);
    assertFalse(customCartRepository.removeItemFromCart(username, skuCode));
  }

  @Test
  public void removeItemFromCartUnitTestPositive() {
    // Check item exist
    String username = "username";
    String skuCode = "MTA-TEST";
    int qty = 5;

    CartDocument.Item item = new CartDocument.Item();
    item.setSkuCode(skuCode);
    item.setQuantity(qty);

    CartDocument.Item item2 = new CartDocument.Item();
    item2.setSkuCode(skuCode + "2");
    item2.setQuantity(qty);

    CartDocument cartDocument = CartDocument.builder()
        .id("1")
        .username(username)
        .items(new ArrayList<>(List.of(item, item2)))
        .build();

    // Check if item size > 1
    Query userCartQuery = new Query(Criteria.where("username").is(username));
    when(mongoTemplate.findOne(userCartQuery, CartDocument.class)).thenReturn(cartDocument);
    Query query = new Query(Criteria.where("username").is(username).and("items.skuCode").is(skuCode));
    Update update = new Update().pull("items", Query.query(Criteria.where("skuCode").is(skuCode)));

    when(mongoTemplate.exists(query, CartDocument.class)).thenReturn(true);
    when(mongoTemplate.updateFirst(query, update, CartDocument.class)).thenReturn(null);
    customCartRepository.removeItemFromCart(username, skuCode);
    verify(mongoTemplate).updateFirst(query, update, CartDocument.class);

    when(mongoTemplate.exists(query, CartDocument.class)).thenReturn(false);
    assertFalse(customCartRepository.removeItemFromCart(username, skuCode));

    reset(mongoTemplate);

    // Check if item == 1
    CartDocument cartDocument2 = CartDocument.builder()
        .id("1")
        .username(username)
        .items(new ArrayList<>(List.of(item)))
        .build();

    when(mongoTemplate.findOne(userCartQuery, CartDocument.class)).thenReturn(cartDocument2);
    when(mongoTemplate.remove(userCartQuery, CartDocument.class)).thenReturn(null);
    assertTrue(customCartRepository.removeItemFromCart(username, skuCode));
    verify(mongoTemplate).remove(userCartQuery, CartDocument.class);

    item.setSkuCode("Wrong SKU");
    CartDocument cartDocument3 = CartDocument.builder()
        .id("1")
        .username(username)
        .items(new ArrayList<>(List.of(item)))
        .build();
    when(mongoTemplate.findOne(userCartQuery, CartDocument.class)).thenReturn(cartDocument3);
    when(mongoTemplate.exists(query, CartDocument.class)).thenReturn(false);

    assertFalse(customCartRepository.removeItemFromCart(username, skuCode));
  }
}
