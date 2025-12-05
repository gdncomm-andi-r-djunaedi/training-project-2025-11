package com.blublu.cart.repository;

import com.blublu.cart.document.CartDocument;
import com.blublu.cart.model.request.EditQtyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class CustomCartRepositoryImpl implements CustomCartRepository {

  @Autowired
  private MongoTemplate mongoTemplate;

  @Override
  public boolean addOrUpdateItem(String username, CartDocument.Item item) {
    // Check if item exists
    Query checkQuery = new Query(Criteria.where("username").is(username).and("items.skuCode").is(item.getSkuCode()));

    boolean itemExists = mongoTemplate.exists(checkQuery, CartDocument.class);

    if (itemExists) {
      // Increment existing item
      Update update = new Update().inc("items.$.quantity", item.getQuantity());
      mongoTemplate.updateFirst(checkQuery, update, CartDocument.class);
    } else {
      // Add new item
      Query userQuery = new Query(Criteria.where("username").is(username));
      Update update = new Update().push("items", item);
      mongoTemplate.upsert(userQuery, update, CartDocument.class);
    }
    return true;
  }

  @Override
  public boolean editCartItem(String username, EditQtyRequest editQtyRequest) {
    Query query =
        new Query(Criteria.where("username").is(username).and("items.skuCode").is(editQtyRequest.getSkuCode()));

    if (mongoTemplate.exists(query, CartDocument.class)) {
      Update update = new Update().set("items.$.quantity", editQtyRequest.getNewQty());
      mongoTemplate.updateFirst(query, update, CartDocument.class);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean removeItemFromCart(String username, String skuCode) {
    Query userCartQuery = new Query(Criteria.where("username").is(username));
    CartDocument cart = mongoTemplate.findOne(userCartQuery, CartDocument.class);

    if (cart == null || cart.getItems() == null) {
      return false;
    }

    if (cart.getItems().size() == 1 && skuCode.equals(cart.getItems().getFirst().getSkuCode())) {
      mongoTemplate.remove(userCartQuery, CartDocument.class);
      return true;
    }

    Query query = new Query(Criteria.where("username").is(username).and("items.skuCode").is(skuCode));

    if (mongoTemplate.exists(query, CartDocument.class)) {
      Update update = new Update().pull("items", Query.query(Criteria.where("skuCode").is(skuCode)));

      mongoTemplate.updateFirst(query, update, CartDocument.class);
      return true;
    } else {
      return false;
    }

  }
}
