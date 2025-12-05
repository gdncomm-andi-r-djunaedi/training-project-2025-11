package com.example.api_gateway.serviceImpl;

import com.example.api_gateway.client.MemberServiceClient;
import com.example.api_gateway.response.AuthResponse;
import com.example.api_gateway.request.LoginRequest;
import com.example.api_gateway.response.MessageResponse;
import com.example.api_gateway.request.RegisterRequest;
import com.example.api_gateway.service.JwtService;
import com.example.api_gateway.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImp implements MemberService {

    @Autowired
    private MemberServiceClient memberServiceClient;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private JwtService jwtService;

    @Override
    public MessageResponse register(RegisterRequest request){
        return memberServiceClient.register(request).getBody();
    }

    @Override
    public MessageResponse login(LoginRequest loginRequest){
        MessageResponse messageResponse = new MessageResponse();
        if(redisTemplate.hasKey(loginRequest.getUserEmail())) {
           messageResponse.setMessage("user already loggedIn");
           return messageResponse;
        }else {
            AuthResponse authResponse = memberServiceClient.login(loginRequest).getBody();
            String token = jwtService.generateToken(authResponse.getUserName(), authResponse.getUserId(),authResponse.getEmail());
            redisTemplate.opsForValue().set(authResponse.getEmail(), token);
            messageResponse.setMessage("user loggedin successfully");
            return messageResponse;
        }
    }

    @Override
    public MessageResponse logout(String email){
        MessageResponse messageResponse = new MessageResponse();
        if(!redisTemplate.hasKey(email)) {
            messageResponse.setMessage("user is not loggedIn or not Registered");
            return messageResponse;
        }else {
            redisTemplate.delete(email);
            messageResponse.setMessage("user loggedout successfully");
            return messageResponse;
        }
    }
}
