package com.microservice.member.controller;

import com.microservice.member.dto.LoginRequestDto;
import com.microservice.member.dto.MemberLogInResponseDto;
import com.microservice.member.dto.RegisterRequestDto;
import com.microservice.member.dto.RegisterResponseDto;
import com.microservice.member.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/member")
public class MemberController {

    @Autowired
    MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> registerUser(@RequestBody RegisterRequestDto registerRequestDto){
        return new ResponseEntity<>(memberService.registerNewUser(registerRequestDto), HttpStatus.OK);
    }

    @PostMapping("/logIn")
    public ResponseEntity<MemberLogInResponseDto> logInUser(@RequestBody LoginRequestDto loginRequestDto){
        return new ResponseEntity<>(memberService.validateUser(loginRequestDto), HttpStatus.OK);
    }


}
