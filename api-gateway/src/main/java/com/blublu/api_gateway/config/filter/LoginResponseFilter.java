package com.blublu.api_gateway.config.filter;

import com.blublu.api_gateway.interfaces.RedisService;
import com.blublu.api_gateway.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class LoginResponseFilter implements WebFilter {

  private final JwtUtil jwtUtil;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  RedisService redisService;

  @Autowired
  public LoginResponseFilter(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    if (exchange.getRequest().getURI().getPath().contains("member/login") && exchange.getRequest()
        .getMethod()
        .equals(HttpMethod.POST)) {

      log.info("Executing {}", LoginResponseFilter.class);
      ServerHttpResponse originalResponse = exchange.getResponse();
      DataBufferFactory bufferFactory = originalResponse.bufferFactory();

      ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
        @Override
        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
          if (Objects.requireNonNull(getStatusCode()).is2xxSuccessful()) {
            if (body instanceof Flux) {
              Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;

              Flux<DataBuffer> newBody = fluxBody.collectList().flatMapMany(list -> {
                DataBuffer joined = bufferFactory.join(list);
                byte[] content = new byte[joined.readableByteCount()];
                joined.read(content);
                DataBufferUtils.release(joined);

                try {
                  Map<String, Object> map = objectMapper.readValue(content, Map.class);
                  if (map.containsKey("username")) {
                    String username = (String) map.get("username");
                    String token = jwtUtil.generateToken(username);
                    map.put("token", token);
                    byte[] newContent = objectMapper.writeValueAsBytes(map);
                    getDelegate().getHeaders().setContentLength(newContent.length);
                    getDelegate().getHeaders().setContentType(MediaType.APPLICATION_JSON);

                    log.info("Creating redis cache with key: {} and value: {}", username, token);
                    redisService.createRedisCache(username, token);
                    return Flux.just(bufferFactory.wrap(newContent));
                  }
                } catch (Exception ignored) {
                  // fall back to original body
                }

                getDelegate().getHeaders().setContentLength(content.length);
                return Flux.just(bufferFactory.wrap(content));
              });

              return super.writeWith(newBody);
            }
          }
          return super.writeWith(body);
        }
      };
      return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }
    return chain.filter(exchange);
  }
}
