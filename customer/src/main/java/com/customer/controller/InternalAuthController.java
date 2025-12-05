package com.customer.controller;

import com.customer.dto.request.LoginRequest;
import com.customer.dto.response.AuthInfoResponse;
import com.customer.dto.response.CustomerResponse;
import com.customer.dto.response.LoginResponse;
import com.customer.entity.CustomerAuth;
import com.customer.repository.CustomerAuthRepository;
import com.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/auth")
@RequiredArgsConstructor
public class InternalAuthController {

    private final CustomerAuthRepository customerAuthRepository;
    private final CustomerService customerService;

    @GetMapping("/customer")
    public ResponseEntity<?> findByEmail(@RequestParam String email) {
        CustomerAuth auth = customerAuthRepository.findByEmailIgnoreCase(email)
                .orElse(null);

        if (auth == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                new AuthInfoResponse(
                        auth.getCustomer().getId(),
                        auth.getEmail(),
                        "ACTIVE"
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        CustomerResponse customer = customerService.validateLogin(
                request.getEmail(),
                request.getPassword()
        );

        return ResponseEntity.ok(
                new LoginResponse(
                        customer.getId().toString(),
                        customer.getEmail()
                )
        );
    }
}
