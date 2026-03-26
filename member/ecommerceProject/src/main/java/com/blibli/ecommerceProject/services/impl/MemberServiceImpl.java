package com.blibli.ecommerceProject.services.impl;

import com.blibli.ecommerceProject.dto.MemberProfiledto;
import com.blibli.ecommerceProject.dto.MemberValidationRequestdto;
import com.blibli.ecommerceProject.dto.Memberdto;
import com.blibli.ecommerceProject.entity.Member;
import com.blibli.ecommerceProject.exception.EmailAlreadyExistsException;
import com.blibli.ecommerceProject.repositories.MemberRepository;
import com.blibli.ecommerceProject.services.MemberService;
import com.blibli.ecommerceProject.util.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    JwtUtil jwtUtil;

    @Override
    public Memberdto registerMember(Memberdto memberdto) {

        String encodedPassword = passwordEncoder.encode(memberdto.getPassword());
        memberdto.setPassword(encodedPassword);
        Member member = new Member();
        BeanUtils.copyProperties(memberdto, member);
        try {
        Member savedMember = memberRepository.save(member);
        return convertToDto(savedMember);
        } catch (Exception ex) {
            throw new RuntimeException("Error saving member.", ex);
        }
    }

    private Memberdto convertToDto(Member member) {
        Memberdto memberdto = new Memberdto();
        BeanUtils.copyProperties(member, memberdto);
        memberdto.setPassword(null);
        return memberdto;
    }


    @Override
    public boolean validateCredentials(MemberValidationRequestdto memberValidationRequestdto) {
        Member member = memberRepository.findByEmailId(memberValidationRequestdto.getEmailId());
        boolean validateResponse = false;
        if (member == null || !passwordEncoder.matches(memberValidationRequestdto.getPassword(), member.getPassword())) {
            return validateResponse;
        }

        validateResponse = true;
        return validateResponse;
    }

    @Override
    public MemberProfiledto getUserProfile(String authToken) {
        if (authToken.startsWith("Bearer ")) {
            authToken = authToken.substring(7).trim();
        }
        String memberId = jwtUtil.getUserNameFromToken(authToken);
        Member member = memberRepository.findByEmailId(memberId);
        if (member == null) {
            return null;
        }

        MemberProfiledto memberProfiledto = memberRepository.findDetailsByEmailId(memberId);
        if (memberProfiledto == null) {
            return null;
        }
        return memberProfiledto;
    }
}
