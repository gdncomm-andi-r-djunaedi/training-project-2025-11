package com.blibli.api_gateway.Feign;

import com.blibli.api_gateway.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "member",url = "http://localhost:8081/api")

public interface MemberFeign {
    @PostMapping("/auth/register")
    public ResponseEntity<UserRegisterResponseDTO> authRegister(@RequestBody UserRegisterRequestDTO userRegisterRequestDTO);

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO);

    @GetMapping("/auth/getUserNameFromToken")
    public ResponseEntity<String> getUserName(@RequestParam String token);

    @GetMapping("/auth/validateToken")
    public ResponseEntity<Boolean> getValidateToken(@RequestParam String token,@RequestParam String userEmail);

    @GetMapping("/auth/logout")
    public ResponseEntity<LogoutResponseDTO> logout(@RequestParam String userName, @RequestParam String token);
}
