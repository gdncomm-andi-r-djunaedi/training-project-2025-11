package com.blibli.memberModule.controller;

import com.blibli.memberModule.dto.CheckUserResponse;
import com.blibli.memberModule.dto.Memberdto;
import com.blibli.memberModule.dto.ValidateMemberRequest;
import com.blibli.memberModule.services.MemberService;
import com.blibli.memberModule.services.impl.TokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    MemberService memberService;

    @PostMapping("/register")
    @Operation(summary = "Create a new member registration")
    public ResponseEntity<Memberdto> register(@RequestBody Memberdto memberdto) {
        log.info("Received registration request for email: {}", memberdto.getEmail());
        Memberdto member = memberService.registerMember(memberdto);
        log.info("Registration completed successfully for email: {}", member.getEmail());
        return new ResponseEntity<>(member, HttpStatus.OK);
    }

    @PostMapping("/validateMember")
    @Operation(summary = "Validate member credentials (email and password)")
    public ResponseEntity<CheckUserResponse> validateMember(@RequestBody ValidateMemberRequest request) {
        log.info("Received member validation request for email: {}", request.getEmail());
        CheckUserResponse response = memberService.validateMember(request.getEmail(), request.getPassword());
        log.info("Member validation completed. Status: {}, Email: {}", response.getStatus(), response.getEmail());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getMemberDetails")
    @Operation(summary = "Get member details by token")
    public ResponseEntity<Memberdto> getMemberDetails(@RequestHeader("Authorization") String authHeader) {
        log.info("Received get member details request");
        String email = TokenUtil.extractEmailFromToken(authHeader);
        log.debug("Extracted email from token: {}", email);
        Memberdto member = memberService.getMemberByEmail(email);
        log.info("Member details retrieved successfully for email: {}", email);
        return new ResponseEntity<>(member, HttpStatus.OK);
    }
}
