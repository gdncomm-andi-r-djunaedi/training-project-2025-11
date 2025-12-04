package com.blibi.apigateway.serviceImpl;

import com.blibi.apigateway.dto.GenericResponse;
import com.blibi.apigateway.dto.LoginRequest;
import com.blibi.apigateway.dto.LoginResponse;
import com.blibi.apigateway.feign.MemberFeignClient;
import com.blibi.apigateway.service.AuthService;
import com.blibi.apigateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberFeignClient memberFeignClient;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Validating login via member service");
        GenericResponse<?> result = memberFeignClient.login(request);
        String token = jwtUtil.generateToken(request.getUserName());
        return LoginResponse.builder()
                .token(token)
                .userName(request.getUserName())
                .build();
    }

    @Override
    public void logout(String token) {
        redisTemplate.opsForValue().set("invalid:" + token, "true");
    }
}
