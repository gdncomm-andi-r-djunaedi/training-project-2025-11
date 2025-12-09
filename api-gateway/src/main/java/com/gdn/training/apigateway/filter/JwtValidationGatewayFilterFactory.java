package com.gdn.training.apigateway.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.gdn.training.apigateway.entity.Member;
import com.gdn.training.apigateway.repository.MemberRepository;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JwtValidationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JwtValidationGatewayFilterFactory.Config> {

    private final MemberRepository memberRepository;
    private final String secret = "testkey";

    public JwtValidationGatewayFilterFactory(MemberRepository memberRepository) {
        super(Config.class);
        this.memberRepository = memberRepository;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);

            try {
                DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secret))
                        .build()
                        .verify(token);

                String username = jwt.getSubject();

                Optional<Member> memberOpt = memberRepository.findByUsername(username);

                if (memberOpt.isPresent()) {
                    Member member = memberOpt.get();

                    if (member.getLastLogout() != null && jwt.getIssuedAt().before(member.getLastLogout())) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                }

                return chain.filter(exchange);

            } catch (Exception e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    public static class Config {
    }
}
