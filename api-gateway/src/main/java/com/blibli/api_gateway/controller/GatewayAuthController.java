package com.blibli.api_gateway.controller;

import com.blibli.api_gateway.dto.*;
import com.blibli.api_gateway.service.GatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class GatewayAuthController {

    @Autowired
    GatewayService gatewayService;
    @PostMapping("/auth/register")
    public ResponseEntity<UserRegisterResponseDTO> authRegister(@RequestBody UserRegisterRequestDTO userRegisterRequestDTO){
        return new ResponseEntity<>(gatewayService.registerUser(userRegisterRequestDTO), HttpStatus.OK);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO){
        return new ResponseEntity<>(gatewayService.login(loginRequestDTO),HttpStatus.OK);
    }


    @GetMapping("/auth/validateToken")
    public ResponseEntity<Boolean> getValidateToken(@RequestParam String token ){
        return new ResponseEntity<>(gatewayService.validateToken(token),HttpStatus.OK);
    }

    @GetMapping("/auth/logout")
    public ResponseEntity<LogoutResponseDTO> logout(@RequestParam String token ){
        return new ResponseEntity<>(gatewayService.logout(token),HttpStatus.OK);
    }
}
