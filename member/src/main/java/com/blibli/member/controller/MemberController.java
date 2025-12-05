package com.blibli.member.controller;

import com.blibli.member.dto.*;
import com.blibli.member.response.ErrorResponse;
import com.blibli.member.response.GdnResponse;
import com.blibli.member.service.UserRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MemberController {

    @Autowired
    UserRegisterService userRegisterService;

    @PostMapping("/auth/register")
    public ResponseEntity<GdnResponse<UserRegisterResponseDTO>> authRegister(@RequestBody UserRegisterRequestDTO userRegisterRequestDTO){
        return new ResponseEntity<>(new GdnResponse(true,null,userRegisterService.registerUser(userRegisterRequestDTO)), HttpStatus.OK);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<GdnResponse<LoginResponseDTO>> login(@RequestBody LoginRequestDTO loginRequestDTO){
        return new ResponseEntity<>(new GdnResponse(true,null,userRegisterService.login(loginRequestDTO)),HttpStatus.OK);
    }

    @GetMapping("/auth/getUserNameFromToken")
    public ResponseEntity<GdnResponse<String>> getUserName(@RequestParam String token){
        return new ResponseEntity<>(new GdnResponse(true,null,userRegisterService.getUserNameFromToken(token)),HttpStatus.OK);
    }

    @GetMapping("/auth/validateToken")
    public ResponseEntity<GdnResponse<Boolean>> getValidateToken(@RequestParam String token,@RequestParam String userEmail){
        return new ResponseEntity<>(new GdnResponse(true,null,userRegisterService.validateToken(token,userEmail)),HttpStatus.OK);
    }

    @GetMapping("/auth/logout")
    public ResponseEntity<GdnResponse<LogoutResponseDTO>> logout(@RequestParam String userEmail, @RequestParam String token){
        return new ResponseEntity<>(new GdnResponse(true,null,userRegisterService.logout(userEmail,token)),HttpStatus.OK);
    }

}
