package com.gdn.member.service.impl;

import com.gdn.member.dto.request.LoginDTO;
import com.gdn.member.dto.response.LoginResponseDTO;
import com.gdn.member.dto.request.MemberRegisterDTO;
import com.gdn.member.entity.Member;
import com.gdn.member.exception.AuthenticationException;
import com.gdn.member.exception.InvalidMemberRegistrationException;
import com.gdn.member.exception.MemberAlreadyExistsException;
import com.gdn.member.repository.MemberRepository;
import com.gdn.member.service.MemberService;
import com.gdn.member.utils.JwtGenerator;
import com.gdn.member.config.SecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final SecurityConfig securityConfig;
    private final JwtGenerator jwtGenerator;
    private final StringRedisTemplate redisTemplate;

    public MemberServiceImpl(MemberRepository memberRepository,
                             SecurityConfig securityConfig,
                             JwtGenerator jwtGenerator,
                             StringRedisTemplate redisTemplate) {
        this.memberRepository = memberRepository;
        this.securityConfig = securityConfig;
        this.jwtGenerator = jwtGenerator;
        this.redisTemplate = redisTemplate;
    }


    @Override
    public HttpStatus saveDetails(MemberRegisterDTO memberRegisterDTO) {
        log.info("Registering member with email={}", memberRegisterDTO.getEmail());

        if (memberRegisterDTO.getFullName() == null ||
                memberRegisterDTO.getEmail() == null ||
                memberRegisterDTO.getPassword() == null ||
                memberRegisterDTO.getPhoneNumber() == null) {

            throw new InvalidMemberRegistrationException(
                    "fullName, email, password and phoneNumber are mandatory"
            );
        }

        if (memberRepository.existsByEmail(memberRegisterDTO.getEmail())) {
            throw new MemberAlreadyExistsException(
                    "Member already exists with email: " + memberRegisterDTO.getEmail()
            );
        }

        Member member = new Member();
        BeanUtils.copyProperties(memberRegisterDTO, member);
        String hashPassword = securityConfig.passwordEncoder()
                .encode(memberRegisterDTO.getPassword());
        member.setPassword(hashPassword);

        memberRepository.save(member);
        log.info("Member registered successfully, email={}", member.getEmail());

        return HttpStatus.CREATED;
    }

    @Override
    public LoginResponseDTO login(LoginDTO loginDTO) {
        log.info("Login attempt for email={}", loginDTO.getEmail());

        if (!memberRepository.existsByEmail(loginDTO.getEmail())) {
            throw new AuthenticationException("Member not found for email: " + loginDTO.getEmail());
        }

        Member member = memberRepository.findByEmail(loginDTO.getEmail());
        boolean passwordMatch = securityConfig.passwordEncoder()
                .matches(loginDTO.getPassword(), member.getPassword());

        if (!passwordMatch) {
            throw new AuthenticationException("Invalid email or password");
        }

        String email = member.getEmail();
        String token = jwtGenerator.generateToken(email);

        String pattern = "*:" + email;
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }

        String redisKey = token + ":" + email;
        redisTemplate.opsForValue().set(redisKey, "ACTIVE");
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setEmail(email);
        loginResponseDTO.setAccessToken(token);

        log.info("Login successful, email={}", email);
        return loginResponseDTO;
    }



    @Override
    public String findPasswordByEmail(String email) {
        return memberRepository.findPasswordByEmail(email);
    }


    @Override
    public HttpStatus logout(String email) {
        log.info("Logout request for email={}", email);

        if (!memberRepository.existsByEmail(email)) {
            throw new AuthenticationException("Member not found for email: " + email);
        }

        String pattern = "*:" + email;
        var keys = redisTemplate.keys(pattern);

        if (keys == null || keys.isEmpty()) {
            throw new AuthenticationException("No active session for email: " + email);
        }

        redisTemplate.delete(keys);
        log.info("Deleted active token(s) for email={}, keys={}", email, keys);

        log.info("Logout successful, email={}", email);
        return HttpStatus.OK;
    }

}
