package com.blibli.member.serviceImplTest;

import com.blibli.member.dto.*;
import com.blibli.member.entity.UserRegister;
import com.blibli.member.repository.UserRegisterRepository;
import com.blibli.member.service.impl.TokenBlackList;
import com.blibli.member.service.impl.UserRegisterServiceImpl;
import com.blibli.member.utils.JWTUtils;
import com.blibli.member.utils.PasswordEncryptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserRegisterServiceImplTest {
    @InjectMocks
    UserRegisterServiceImpl userRegisterService;

    @Mock
    UserRegisterRepository userRegisterRepository;
    @Mock
    PasswordEncryptor passwordEncryptor;
    @Mock
    BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    JWTUtils jwtUtils;
    @Mock
    TokenBlackList tokenBlackList;

    @Test
    void test_RegisterUser() {
        UserRegisterRequestDTO userRegisterRequestDTO = new UserRegisterRequestDTO();
        userRegisterRequestDTO.setUserEmail("test@gmail.com");
        userRegisterRequestDTO.setPassword("1234");
//		userRegisterRequestDTO.setUserDOB(new Date("2025-12-02T13:30:56.048Z"));
        userRegisterRequestDTO.setMobileNo("9348232");


        Mockito.when(userRegisterRepository.isexistByEmail(userRegisterRequestDTO.getUserEmail())).thenReturn(true);

        try {
            userRegisterService.registerUser(userRegisterRequestDTO);
        } catch (RuntimeException ex) {
            Assertions.assertTrue(ex.getMessage().contains("Useremail id is already exists"));
        }

        UserRegister userRegister = new UserRegister();
        userRegister.setUserEmail("test@gmail.com");
        userRegister.setPassword("encode1234");
        userRegister.setMobileNo("9348232");

        Mockito.when(userRegisterRepository.isexistByEmail(userRegisterRequestDTO.getUserEmail())).thenReturn(false);
        Mockito.when(passwordEncryptor.encoder()).thenReturn(bCryptPasswordEncoder);
        Mockito.when(passwordEncryptor.encoder().encode("1234")).thenReturn("encode1234");
        Mockito.when(userRegisterRepository.save(userRegister)).thenReturn(userRegister);

        UserRegisterResponseDTO response =userRegisterService.registerUser(userRegisterRequestDTO);

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.getUserEmail().contains("test@gmail.com"));
        Assertions.assertTrue(!response.getMobileNo().isEmpty());

        Mockito.verify(userRegisterRepository, Mockito.times(2)).isexistByEmail(userRegisterRequestDTO.getUserEmail());

    }

    @Test
    void test_Login(){
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setUserEmail("adhfh@gmail.com");
        loginRequestDTO.setPassword("1234");

        Mockito.when(userRegisterRepository.isexistByEmail(loginRequestDTO.getUserEmail())).thenReturn(true);
        Mockito.when(passwordEncryptor.encoder()).thenReturn(bCryptPasswordEncoder);
        Mockito.when(userRegisterRepository.getPasswordByUserName(loginRequestDTO.getUserEmail())).thenReturn("1234");
        Mockito.when(passwordEncryptor.encoder().matches(loginRequestDTO.getPassword(),userRegisterRepository.getPasswordByUserName(loginRequestDTO.getUserEmail()))).thenReturn(true);
        Mockito.when(jwtUtils.generateToken(loginRequestDTO.getUserEmail())).thenReturn("12345");
        LoginResponseDTO loginResponseDTO = userRegisterService.login(loginRequestDTO);

        Assertions.assertNotNull(loginResponseDTO);
        Assertions.assertNotNull(loginResponseDTO.getToken());

        Mockito.verify(userRegisterRepository, Mockito.times(1)).isexistByEmail(loginRequestDTO.getUserEmail());
        Mockito.verify(userRegisterRepository, Mockito.times(2)).getPasswordByUserName(loginRequestDTO.getUserEmail());
    }

    @Test
    void test_getUserNameFromToken(){
        String token = "123456";
        Mockito.when(jwtUtils.extractUsername(token)).thenReturn("asdf@gmail.com");

        String email = userRegisterService.getUserNameFromToken(token);

        Assertions.assertTrue(email.contains("asdf@gmail.com"));

        Mockito.verify(jwtUtils,Mockito.times(1)).extractUsername(token);

    }

    @Test
    void test_validateToken(){
        String token = "123456";
        String email = "asdf@gmail.com";
        Mockito.when(jwtUtils.validateToken(email,token)).thenReturn(true);

        Boolean response = userRegisterService.validateToken(token,email);

        Assertions.assertTrue(response);
        Mockito.verify(jwtUtils,Mockito.times(1)).validateToken(email,token);
    }
    @Test
    void test_logout(){
        String token = "123456";
        String email = "asdf@gmail.com";

        LogoutResponseDTO logoutResponseDTO = userRegisterService.logout(email,token);

        Assertions.assertTrue(logoutResponseDTO.getMessage().contains("UserLogged out"));

    }

}
