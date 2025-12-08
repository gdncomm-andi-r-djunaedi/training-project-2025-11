package MemberService.MemberService.service.impl;

import MemberService.MemberService.dto.LoginDto;
import MemberService.MemberService.dto.RegisterDto;
import MemberService.MemberService.entity.Member;
import MemberService.MemberService.exception.InvalidPasswordException;
import MemberService.MemberService.exception.UserAlreadyExistsException;
import MemberService.MemberService.exception.UserNotFoundException;
import MemberService.MemberService.repository.MemberRepository;
import MemberService.MemberService.security.JWTUtil;
import MemberService.MemberService.service.MemberService;
import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;

import java.util.UUID;


@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JWTUtil jwtUtil;

    @Override
    public Member register(RegisterDto registerDto){
        memberRepository.findByEmail(registerDto.getEmail())
                .ifPresent(x -> {throw new UserAlreadyExistsException("Email Already exists");});
        Member member =new Member();
        BeanUtils.copyProperties(registerDto,member);
        member.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        return memberRepository.save(member);
    }

    @Override
    public String login(LoginDto loginDto){
      Member member = memberRepository.findByEmail(loginDto.getEmail())
              .orElseThrow(() -> new UserNotFoundException("User not found"));

      if(!passwordEncoder.matches(loginDto.getPassword(),member.getPassword())){
         throw new InvalidPasswordException("Password is invalid");
        }
      return jwtUtil.generateToken((member.getId()));
    }

    @Override
    public Member getProfile(String userId){
        return memberRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("User didnt exist"));
    }

    @Override
    public String bulkRegister(int count) {

        Faker faker = new Faker();

        for (int i = 0; i < count; i++) {
            RegisterDto dto = new RegisterDto();
            dto.setFullName(faker.name().fullName());
            dto.setEmail(faker.internet().emailAddress());
            dto.setPassword("Password123");

            register(dto);
        }

        return count + " users registered successfully!";
    }


}
