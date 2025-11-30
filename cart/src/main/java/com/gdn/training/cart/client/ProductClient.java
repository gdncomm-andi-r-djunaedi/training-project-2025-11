package com.gdn.training.cart.client;

import com.gdn.training.cart.config.ServiceClientConfig;
import com.gdn.training.cart.config.ServiceClientsProperties;
import com.gdn.training.cart.dto.ProductDTO;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Component
public class ProductClient {

    private final RestTemplate restTemplate;
    private final ServiceClientsProperties serviceClientsProperties;

    public ProductClient(RestTemplateBuilder builder, ServiceClientsProperties serviceClientsProperties) {
        this.serviceClientsProperties = serviceClientsProperties;
        ServiceClientConfig config = serviceClientsProperties.getRequired("product");

        this.restTemplate = builder
                .connectTimeout(Duration.ofMillis(config.getConnectTimeout()))
                .readTimeout(Duration.ofMillis(config.getReadTimeout()))
                .build();
    }

    public ProductDTO getProductById(String productId) {
        ServiceClientConfig config = serviceClientsProperties.getRequired("product");
        String detailEndpoint = config.getEndpoints().get("detail");
        String url = config.getBaseUrl() + detailEndpoint.replace("{id}", productId);
        return restTemplate.getForObject(url, ProductDTO.class);
    }
}
