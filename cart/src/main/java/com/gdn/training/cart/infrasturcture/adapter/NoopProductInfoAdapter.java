package com.gdn.training.cart.infrasturcture.adapter;

import com.gdn.training.cart.application.dto.ProductInfoResponse;
import com.gdn.training.cart.application.port.out.ProductInfoPort;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Component
public class NoopProductInfoAdapter implements ProductInfoPort {

    @Override
    public Map<UUID, ProductInfoResponse> fetchProductInfo(Collection<UUID> productIds) {
        return Map.of(); // empty map; no data needed in Phase 0
    }
}