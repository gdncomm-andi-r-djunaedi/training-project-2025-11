package com.gdn.member.controller;

import com.gdn.member.dto.request.LoginDTO;
import com.gdn.member.dto.request.MemberRegisterDTO;
import com.gdn.member.dto.response.ApiResponse;
import com.gdn.member.dto.response.LoginResponseDTO;
import com.gdn.member.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/members")
public class MemberServiceController {

    private final MemberService memberService;

    public MemberServiceController(MemberService memberService) {
        this.memberService = memberService;
    }


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<MemberRegisterDTO>> register(@RequestBody MemberRegisterDTO dto) {
        log.info("Register request: {}", dto.getEmail());

        memberService.saveDetails(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member registered successfully", dto));
    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@RequestBody LoginDTO loginDTO) {
        log.info("Login request for email={}", loginDTO.getEmail());

        LoginResponseDTO loginResponseDTO = memberService.login(loginDTO);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", loginResponseDTO)
        );
    }


    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestParam String email) {
        log.info("Logout request for email={}", email);

        memberService.logout(email);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("User logged out successfully", email));
    }
}
