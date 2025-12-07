package com.MarketPlace.MemberService.service;

import com.MarketPlace.MemberService.dto.MemberDetailDto;
import com.MarketPlace.MemberService.dto.MemberLoginRequestDTO;
import com.MarketPlace.MemberService.dto.MemberLoginResponseDTO;
import com.MarketPlace.MemberService.dto.MemberResponseDTO;
import com.MarketPlace.MemberService.entity.Member;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

public interface MemberService {

    MemberResponseDTO register(MemberResponseDTO memberResponseDTO);

    MemberLoginResponseDTO login(MemberLoginRequestDTO request) throws NoSuchAlgorithmException, InvalidKeySpecException;

    Optional<MemberDetailDto> getMemberProfile(Long memberId);
}
