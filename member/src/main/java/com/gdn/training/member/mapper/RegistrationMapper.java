package com.gdn.training.member.mapper;

import com.gdn.training.member.model.entity.User;
import com.gdn.training.member.model.request.RegistrationRequest;
import com.gdn.training.member.model.response.RegistrationResponse;

import org.springframework.stereotype.Component;

@Component
public class RegistrationMapper {
    public User toUser(RegistrationRequest request) {
        return User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .name(request.getName())
                .build();
    }

    public RegistrationResponse toRegistrationResponse(
            final User user) {

        return new RegistrationResponse(
                user.getEmail(),
                "User registered successfully");
    }
}
