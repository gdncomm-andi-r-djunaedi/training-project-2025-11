package com.gdn.training.apigateway.application.port;

import com.gdn.training.apigateway.application.usecase.model.TokenData;

import java.util.Map;

public interface JwtTokenPort {

    /**
     * Generate a JWT token for the given user ID.
     *
     * @param userId the user ID
     * @return the generated JWT token
     */
    TokenData createToken(
            String subject,
            Map<String, Object> claims,
            long expiration);

}
