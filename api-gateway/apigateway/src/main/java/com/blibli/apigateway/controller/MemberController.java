package com.blibli.apigateway.controller;

import com.blibli.apigateway.client.MemberClient;
import com.blibli.apigateway.dto.request.MemberDto;
import com.blibli.apigateway.dto.response.MemberErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/members")
@Tag(name = "Member", description = "Member management API")
public class MemberController {
    private final MemberClient memberClient;

    public MemberController(MemberClient memberClient, ObjectMapper realObjectMapper) {
        this.memberClient = memberClient;
    }

    @PostMapping("/register")
    @Operation(summary = "Register New Member", description = "Create a new member registration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member registered successfully",
                    content = @Content(schema = @Schema(implementation = MemberDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - Validation error",
                    content = @Content(schema = @Schema(implementation = MemberErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict - Email already exists",
                    content = @Content(schema = @Schema(implementation = MemberErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<?> register(@RequestBody MemberDto memberDto) {
        log.info("Registering member with email: {}", memberDto.getEmail());
        MemberDto registeredMember = memberClient.register(memberDto);
        log.info("Member registered successfully with email: {}", registeredMember.getEmail());
        return ResponseEntity.ok(registeredMember);
    }
}
