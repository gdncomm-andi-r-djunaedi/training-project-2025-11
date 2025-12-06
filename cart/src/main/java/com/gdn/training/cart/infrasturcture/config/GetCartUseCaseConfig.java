package com.gdn.training.cart.infrasturcture.config;

import com.gdn.training.cart.application.port.out.CartRepositoryPort;
import com.gdn.training.cart.application.port.out.ProductInfoPort;
import com.gdn.training.cart.application.usecase.GetCartUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GetCartUseCaseConfig {

    @Bean
    public GetCartUseCase getCartUseCase(CartRepositoryPort cartRepositoryPort, ProductInfoPort productInfoPort) {
        return new GetCartUseCase(cartRepositoryPort, productInfoPort);
    }
}
