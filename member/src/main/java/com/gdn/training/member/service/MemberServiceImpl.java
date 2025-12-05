package com.gdn.training.member.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.gdn.training.member.dto.LoginResponse;
import com.gdn.training.member.dto.LogoutResponse;
import com.gdn.training.member.dto.RegisterResponse;
import com.gdn.training.member.entity.Member;
import com.gdn.training.member.exception.InvalidCredentialsException;
import com.gdn.training.member.exception.InvalidTokenException;
import com.gdn.training.member.exception.MemberNotFoundException;
import com.gdn.training.member.exception.UserAlreadyExistsException;
import com.gdn.training.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static com.auth0.jwt.JWT.decode;

@Service
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public MemberServiceImpl(MemberRepository memberRepository, PasswordEncoder passwordEncoder,
            TokenService tokenService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Override
    public RegisterResponse register(String username, String email, String password) {
        if (memberRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username already exists: " + username);
        }
        if (memberRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email already exists: " + email);
        }

        Member member = new Member(username, email, passwordEncoder.encode(password));
        memberRepository.save(member);

        return new RegisterResponse();
    }

    @Override
    public LoginResponse login(String username, String password) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException());

        if (!passwordEncoder.matches(password, member.getHashPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = tokenService.generateToken(username);
        return new LoginResponse(token);
    }

    @Override
    public LogoutResponse logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException();
        }

        String token = authorizationHeader.substring(7);
        DecodedJWT jwt = decode(token);
        String username = jwt.getSubject();

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberNotFoundException(username));

        member.setLastLogout(new Date());
        memberRepository.save(member);

        return new LogoutResponse();
    }
}
