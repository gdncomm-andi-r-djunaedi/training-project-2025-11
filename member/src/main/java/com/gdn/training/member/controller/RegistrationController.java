package com.gdn.training.member.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.gdn.training.member.service.RegistrationService;
import com.gdn.training.member.mapper.RegistrationMapper;
import com.gdn.training.member.model.request.RegistrationRequest;
import com.gdn.training.member.model.response.RegistrationResponse;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationService registrationService;
    private final RegistrationMapper registrationMapper;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        final var registeredUser = registrationService.register(request);
        return ResponseEntity.ok(registrationMapper.toRegistrationResponse(registeredUser));
    }

}
