package com.blibli.ecommerceProject.controller;


import com.blibli.ecommerceProject.dto.MemberProfiledto;
import com.blibli.ecommerceProject.dto.MemberValidationRequestdto;
import com.blibli.ecommerceProject.dto.Memberdto;
import com.blibli.ecommerceProject.services.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/members"})
@Tag(name = "Member Management", description = "API's for managing member")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new member", description = "Registeration of new member")
    public ResponseEntity<Memberdto> registerMember(@Valid @RequestBody Memberdto memberdto) {
        Memberdto createdMember = memberService.registerMember(memberdto);
        return new ResponseEntity<>(createdMember, HttpStatus.CREATED);
    }

    @PostMapping("/validateMember")
    @Operation(summary = "Validate member credentials", description = "Validation of member creds")
    public boolean validateMember(@RequestBody MemberValidationRequestdto memberValidationRequestdto) {
        return memberService.validateCredentials(memberValidationRequestdto);
    }

    @GetMapping("/getUserProfile")
    @Operation(summary = "Get profile details", description = "Get profile details")
    public MemberProfiledto getUserProfile(@RequestHeader(value = "Authorization",required = true) String authToken) {
        return memberService.getUserProfile(authToken);
    }
}
