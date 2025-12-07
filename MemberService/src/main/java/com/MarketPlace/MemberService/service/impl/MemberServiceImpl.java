package com.MarketPlace.MemberService.service.impl;

import com.MarketPlace.MemberService.dto.MemberDetailDto;
import com.MarketPlace.MemberService.dto.MemberLoginRequestDTO;
import com.MarketPlace.MemberService.dto.MemberLoginResponseDTO;
import com.MarketPlace.MemberService.dto.MemberResponseDTO;
import com.MarketPlace.MemberService.entity.Member;
import com.MarketPlace.MemberService.exceptions.MemberServiceException;
import com.MarketPlace.MemberService.hashPassword.PasswordHashUtil;
import com.MarketPlace.MemberService.repository.MemberRepository;
import com.MarketPlace.MemberService.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

@Slf4j
@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    MemberRepository memberRepository;

    @Override
    public MemberResponseDTO register(MemberResponseDTO memberResponseDTO) {
        if (memberResponseDTO == null) {
            throw new MemberServiceException("Member request cannot be null", HttpStatus.BAD_REQUEST);
        }

        try{
        Member member = new Member();
        BeanUtils.copyProperties(memberResponseDTO,member);

        byte[] salt = PasswordHashUtil.generateSalt();
        String hashedPasswordAndSalt = PasswordHashUtil.hashPassword(
                memberResponseDTO.getPassword(),
                salt
        );
        member.setPassword(hashedPasswordAndSalt);

        Member sameMember = memberRepository.save(member);
        return convertToDTO(sameMember);
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e)
        {
            throw new MemberServiceException("Failed to register member due to a system security error.", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public MemberLoginResponseDTO login(MemberLoginRequestDTO request) throws NoSuchAlgorithmException, InvalidKeySpecException {

        Member member = memberRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new MemberServiceException("User not found",HttpStatus.UNAUTHORIZED));

        if (!PasswordHashUtil.verifyPassword(request.getPassword(), member.getPassword())) {
            throw new MemberServiceException("Invalid username or password",HttpStatus.UNAUTHORIZED);
        }

        return new MemberLoginResponseDTO(member.getUsername(), null);
    }

    @Override
    public Optional<MemberDetailDto> getMemberProfile(Long memberId) {
        Optional<Member> memberOptional = memberRepository.findById(memberId.toString());

        return memberOptional.map(member -> new MemberDetailDto(
                member.getUsername(),
                member.getEmail()
        ));
    }

    private MemberResponseDTO convertToDTO(Member member) {
        MemberResponseDTO memberResponseDTO = new MemberResponseDTO();
        BeanUtils.copyProperties(member, memberResponseDTO);
        return memberResponseDTO;
    }
}
