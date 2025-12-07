package com.MarketPlace.MemberService.controller;

import com.MarketPlace.MemberService.dto.MemberDetailDto;
import com.MarketPlace.MemberService.dto.MemberLoginRequestDTO;
import com.MarketPlace.MemberService.dto.MemberLoginResponseDTO;
import com.MarketPlace.MemberService.dto.MemberResponseDTO;
import com.MarketPlace.MemberService.exceptions.MemberServiceException;
import com.MarketPlace.MemberService.repository.MemberRepository;
import com.MarketPlace.MemberService.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

@RestController
@RequestMapping("/member")
public class MemberController {

    @Autowired
    MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<MemberResponseDTO> registerMember(@Valid @RequestBody MemberResponseDTO memberResponseDTO) {
        try {
            MemberResponseDTO member = memberService.register(memberResponseDTO);
            return new ResponseEntity<>(member, HttpStatus.CREATED);
        }
        catch (MemberServiceException e) {
            throw new MemberServiceException("Request body is not correct", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<MemberLoginResponseDTO> login(@RequestBody MemberLoginRequestDTO request) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return ResponseEntity.ok(memberService.login(request));
    }

//    @GetMapping("/getMemberProfile/{memberId}")
//    public ResponseEntity<MemberDetailDto> getMemberProfile(@PathVariable Long memberId) {
//        Optional<MemberDetailDto> member = memberService.getMemberProfile(memberId);
//        if (member.isPresent() && !member.isEmpty()) {
//            return ResponseEntity.ok(member.get());
//        }
//        else {
//            throw new MemberServiceException("No member found with ID: " + memberId, HttpStatus.NOT_FOUND);
//        }
//    }
}
