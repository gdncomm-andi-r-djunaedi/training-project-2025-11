package com.gdn.project.waroenk.cart.service;

import com.gdn.project.waroenk.cart.FilterCartRequest;
import com.gdn.project.waroenk.cart.MultipleCartResponse;
import com.gdn.project.waroenk.cart.entity.Cart;
import com.gdn.project.waroenk.cart.entity.CartItem;
import com.gdn.project.waroenk.cart.exceptions.ResourceNotFoundException;
import com.gdn.project.waroenk.cart.mapper.CartMapper;
import com.gdn.project.waroenk.cart.repository.CartRepository;
import com.gdn.project.waroenk.cart.repository.MongoPageAble;
import com.gdn.project.waroenk.cart.repository.model.ResultData;
import com.gdn.project.waroenk.cart.utility.CacheUtil;
import com.gdn.project.waroenk.cart.utility.ParserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CartServiceImpl extends MongoPageAble<Cart, String> implements CartService {

    private static final CartMapper mapper = CartMapper.INSTANCE;
    private static final String CART_PREFIX = "cart";
    private final CartRepository repository;
    private final CacheUtil<Cart> cacheUtil;

    @Value("${default.item-per-page:10}")
    private Integer defaultItemPerPage;

    public CartServiceImpl(CartRepository repository,
                           CacheUtil<Cart> cacheUtil,
                           CacheUtil<String> stringCacheUtil,
                           MongoTemplate mongoTemplate) {
        super(CART_PREFIX, stringCacheUtil, mongoTemplate, 10, TimeUnit.MINUTES, Cart.class);
        this.repository = repository;
        this.cacheUtil = cacheUtil;
    }

    @Override
    public Cart getCart(String userId) {
        String key = CART_PREFIX + ":user:" + userId;
        Cart cached = cacheUtil.getValue(key);
        if (ObjectUtils.isNotEmpty(cached)) {
            return cached;
        }

        Cart cart = repository.findByUserId(userId)
                .orElseGet(() -> {
                    // Create empty cart if not exists
                    Cart newCart = Cart.builder()
                            .userId(userId)
                            .items(new ArrayList<>())
                            .currency("IDR")
                            .build();
                    return repository.save(newCart);
                });
        
        cacheUtil.putValue(key, cart, 1, TimeUnit.HOURS);
        return cart;
    }

    @Override
    public Cart addItem(String userId, CartItem item) {
        Cart cart = getCart(userId);
        cart.addOrUpdateItem(item);
        Cart saved = repository.save(cart);
        updateCache(saved);
        return saved;
    }

    @Override
    public Cart bulkAddItems(String userId, List<CartItem> items) {
        Cart cart = getCart(userId);
        for (CartItem item : items) {
            cart.addOrUpdateItem(item);
        }
        Cart saved = repository.save(cart);
        updateCache(saved);
        return saved;
    }

    @Override
    public Cart removeItem(String userId, String sku) {
        Cart cart = getCart(userId);
        boolean removed = cart.removeItem(sku);
        if (!removed) {
            throw new ResourceNotFoundException("Item with SKU " + sku + " not found in cart");
        }
        Cart saved = repository.save(cart);
        updateCache(saved);
        return saved;
    }

    @Override
    public Cart bulkRemoveItems(String userId, List<String> skus) {
        Cart cart = getCart(userId);
        for (String sku : skus) {
            cart.removeItem(sku);
        }
        Cart saved = repository.save(cart);
        updateCache(saved);
        return saved;
    }

    @Override
    public Cart updateItemQuantity(String userId, String sku, Integer quantity) {
        Cart cart = getCart(userId);
        boolean updated = cart.updateItemQuantity(sku, quantity);
        if (!updated) {
            throw new ResourceNotFoundException("Item with SKU " + sku + " not found in cart");
        }
        Cart saved = repository.save(cart);
        updateCache(saved);
        return saved;
    }

    @Override
    public boolean clearCart(String userId) {
        Cart cart = getCart(userId);
        cart.clearItems();
        repository.save(cart);
        invalidateCache(userId);
        return true;
    }

    @Override
    public MultipleCartResponse filterCarts(FilterCartRequest request) {
        int size = request.getSize() > 0 ? request.getSize() : defaultItemPerPage;

        CriteriaBuilder criteriaBuilder = () -> {
            List<Criteria> criteriaList = new ArrayList<>();
            if (StringUtils.isNotBlank(request.getUserId())) {
                criteriaList.add(Criteria.where("userId").regex(request.getUserId(), "i"));
            }
            return criteriaList;
        };

        ResultData<Cart> entries = query(criteriaBuilder, size, request.getCursor(),
                mapper.toSortByDto(request.getSortBy()));
        Long total = entries.getTotal();
        String nextToken = null;
        Optional<Cart> offset = entries.getOffset();
        if (offset.isPresent()) {
            nextToken = ParserUtil.encodeBase64(offset.get().getId());
        }

        MultipleCartResponse.Builder builder = MultipleCartResponse.newBuilder();
        entries.getDataList().iterator().forEachRemaining(item -> builder.addData(mapper.toResponseGrpc(item)));
        if (StringUtils.isNotBlank(nextToken)) {
            builder.setNextToken(nextToken);
        }
        builder.setTotal(ObjectUtils.isNotEmpty(total) ? total.intValue() : 0);
        return builder.build();
    }

    private void updateCache(Cart cart) {
        String key = CART_PREFIX + ":user:" + cart.getUserId();
        cacheUtil.putValue(key, cart, 1, TimeUnit.HOURS);
    }

    private void invalidateCache(String userId) {
        String key = CART_PREFIX + ":user:" + userId;
        cacheUtil.removeValue(key);
    }

    @Override
    protected String toId(String input) {
        return input;
    }

    @Override
    protected String getId(Cart input) {
        return input.getId();
    }

    @Override
    protected String getIdFieldName() {
        return "_id";
    }
}




