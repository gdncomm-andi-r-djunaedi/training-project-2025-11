package com.blibli.member.service.impl;

import com.blibli.member.dto.*;
import com.blibli.member.entity.UserRegister;
import com.blibli.member.repository.UserRegisterRepository;
import com.blibli.member.service.UserRegisterService;
import com.blibli.member.utils.JWTUtils;
import com.blibli.member.utils.PasswordEncryptor;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.security.KeyRep.Type.SECRET;

@Service
@Slf4j
public class UserRegisterServiceImpl implements UserRegisterService {
    @Autowired
    UserRegisterRepository userRegisterRepository;
    @Autowired
    PasswordEncryptor passwordEncryptor;
    @Autowired
    JWTUtils jwtUtils;
    @Autowired
    TokenBlackList tokenBlackList;

    @Transactional
    @Override
    public UserRegisterResponseDTO registerUser(UserRegisterRequestDTO userRegisterRequestDTO) {
        if (userRegisterRepository.isexistByEmail(userRegisterRequestDTO.getUserEmail())) {
            throw new RuntimeException("Useremail id is already exists");
        }
        if(userRegisterRequestDTO.getPassword().isEmpty() || userRegisterRequestDTO.getPassword().length()>8 || userRegisterRequestDTO.getPassword().length()<4)
            throw new RuntimeException("User password length should be greater than 4 and less than 8");
        if(userRegisterRequestDTO.getUserDOB().after(new Date())){
            throw new RuntimeException("User DOB field is wrong");
        }
        validateEmailId(userRegisterRequestDTO.getUserEmail());
        userRegisterRequestDTO.setPassword(passwordEncryptor.encoder().encode(userRegisterRequestDTO.getPassword()));
        UserRegister userRegister = convertFromDTO(userRegisterRequestDTO);
        log.info("Registering the user to member DB");
        UserRegister response = userRegisterRepository.save(userRegister);
        log.info("Registering completed user to member DB");
        UserRegisterResponseDTO responseDTO = new UserRegisterResponseDTO();
        BeanUtils.copyProperties(response, responseDTO);
        return responseDTO;
    }


    private void validateEmailId(String userEmail) {
        if(!(userEmail.contains("@")&&userEmail.contains(".com"))){
            throw new RuntimeException("Invalid user emailId");
        }
    }

    @Transactional
    @Override
    @Cacheable(value = "users", key = "#loginRequestDTO.userEmail", condition = "#loginRequestDTO.userEmail!=null")
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        if(!userRegisterRepository.isexistByEmail(loginRequestDTO.getUserEmail())) {
            throw new RuntimeException("Invalid username");
        }
        if(!passwordEncryptor.encoder().matches(loginRequestDTO.getPassword(),userRegisterRepository.getPasswordByUserName(loginRequestDTO.getUserEmail()))){
            throw new RuntimeException("Wrong password");
        }
        validateEmailId(loginRequestDTO.getUserEmail());
        log.info("Generating the token for member user"+loginRequestDTO.getUserEmail());
        String token= jwtUtils.generateToken(loginRequestDTO.getUserEmail());
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setToken(token);
        return loginResponseDTO;
    }

    @Transactional
    @Override
    public String getUserNameFromToken(String token) {
        return jwtUtils.extractUsername(token);
    }

    @Transactional
    @Override
    public Boolean validateToken(String token, String userEmail) {
        return jwtUtils.validateToken(userEmail,token);
    }

    @Transactional
    @Override
    @CacheEvict(value = "users", key = "#userEmail", condition = "#userEmail != null")
    public LogoutResponseDTO logout(String userEmail, String token) {
        validateEmailId(userEmail);
//        add validation check from redis
        log.info("Adding token to blocklisted service");
        tokenBlackList.addBlockListToken(token);
        LogoutResponseDTO logoutResponseDTO = new LogoutResponseDTO();
        logoutResponseDTO.setMessage("UserLogged out");
        return logoutResponseDTO;
    }


    private UserRegisterRequestDTO convertFromService(UserRegister response) {
        UserRegisterRequestDTO responseDTO = new UserRegisterRequestDTO();
        BeanUtils.copyProperties(response, responseDTO);
        return responseDTO;
    }

    private UserRegister convertFromDTO(UserRegisterRequestDTO userRegisterRequestDTO) {
        UserRegister userRegister = new UserRegister();
        BeanUtils.copyProperties(userRegisterRequestDTO, userRegister);
        return userRegister;
    }
}
