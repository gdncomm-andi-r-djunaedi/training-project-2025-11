package org.edmund.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.edmund.member.dto.LoginMemberDto;
import org.edmund.member.dto.RegisterMemberDto;
import org.edmund.member.entity.Member;
import org.edmund.commonlibrary.response.GenericResponse;
import org.edmund.member.response.GetMemberResponse;
import org.edmund.member.response.LoginResponse;
import org.edmund.member.services.MemberService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/register")
    @Operation(summary = "Register Account")
    public GenericResponse<Member> registerAccount(@RequestBody RegisterMemberDto request) {
        try {
            Member createdMember = memberService.registerMember(request);
            return GenericResponse.ok(createdMember);
        } catch(Exception e) {
            return GenericResponse.badRequest(e.getMessage());
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login Account")
    public GenericResponse<LoginResponse> loginAccount(@RequestBody LoginMemberDto request) {
        try {
            LoginResponse createdMember = memberService.loginMember(request);
            return GenericResponse.ok(createdMember);
        } catch(Exception e) {
            return GenericResponse.badRequest(e.getMessage());
        }
    }

    @GetMapping("/findAccount")
    @Operation(summary = "Find Account by Email")
    public GenericResponse<GetMemberResponse> findByEmail(@RequestParam String email) {
        return memberService.findByEmail(email)
                .map(member -> GenericResponse.ok(member))
                .orElse(GenericResponse.notFound("No member found with email : " + email));
    }


}
