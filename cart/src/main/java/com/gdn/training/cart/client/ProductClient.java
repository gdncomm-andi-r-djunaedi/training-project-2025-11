package com.gdn.training.cart.client;

import com.gdn.training.cart.config.ServiceClientConfig;
import com.gdn.training.cart.config.ServiceClientsProperties;
import com.gdn.training.cart.dto.ProductDTO;
import com.gdn.training.common.model.BaseResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
        ResponseEntity<BaseResponse<ProductDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<BaseResponse<ProductDTO>>() {}
        );

        BaseResponse<ProductDTO> body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("Product service returned an empty response");
        }
        if (!body.isSuccess()) {
            throw new IllegalArgumentException(body.getMessage() != null
                    ? body.getMessage()
                    : "Product service rejected the request");
        }
        if (body.getData() == null) {
            throw new IllegalStateException("Product service returned no product data");
        }

        return body.getData();
    }
}
