package com.blibli.member.serviceImplTest;

import com.blibli.member.controller.MemberController;
import com.blibli.member.dto.*;
import com.blibli.member.response.GdnResponse;
import com.blibli.member.service.UserRegisterService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class MemberControllerTest {

    @InjectMocks
    MemberController memberController;

    @Mock
    UserRegisterService userRegisterService;

    @Test
    public void test_authRegister(){
        UserRegisterRequestDTO userRegisterRequestDTO = new UserRegisterRequestDTO();
        userRegisterRequestDTO.setUserEmail("test@gmail.com");
        userRegisterRequestDTO.setUserName("test");
        userRegisterRequestDTO.setPassword("1234");
        userRegisterRequestDTO.setMobileNo("9448319112");

        UserRegisterResponseDTO userRegisterResponseDTO = new UserRegisterResponseDTO();
        BeanUtils.copyProperties(userRegisterRequestDTO,userRegisterResponseDTO);

        Mockito.when(userRegisterService.registerUser(userRegisterRequestDTO)).thenReturn(userRegisterResponseDTO);

        ResponseEntity<GdnResponse<UserRegisterResponseDTO>> response = memberController.authRegister(userRegisterRequestDTO);
        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.getBody().getData().getUserName().contains("test"));
        Assertions.assertTrue(response.getBody().getData().getUserEmail().contains("test@gmail.com"));
        Assertions.assertTrue(response.getBody().getData().getMobileNo().contains("9448319112"));

    }

    @Test
    public void test_login(){
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setUserEmail("asd@gmail.com");
        loginRequestDTO.setPassword("1234");

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setToken("1234567890");

        Mockito.when(userRegisterService.login(loginRequestDTO)).thenReturn(loginResponseDTO);

        ResponseEntity<GdnResponse<LoginResponseDTO>> response = memberController.login(loginRequestDTO);

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.getBody().getData().getToken().contains("1234567890"));

    }

    @Test
    public void test_getUserName(){
        String token = "123456789";

        Mockito.when(userRegisterService.getUserNameFromToken(token)).thenReturn("asdf@gmail.com");

        ResponseEntity<GdnResponse<String>> response = memberController.getUserName(token);
        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.getBody().getData().contains("asdf@gmail.com"));
    }

    @Test
    public void test_getValidateToken(){
        String token1 = "1234567890";
        String userEmail1 = "asd@gmail.com";

        String token2 = "123456789";
        String userEmail2 = "asdf@gmail.com";
        Mockito.when(userRegisterService.validateToken(token1,userEmail1)).thenReturn(false);

        ResponseEntity<GdnResponse<Boolean>> response = memberController.getValidateToken(token1,userEmail1);

        Assertions.assertTrue(response.getBody().getData()==false);

        Mockito.when(userRegisterService.validateToken(token2,userEmail2)).thenReturn(true);

        ResponseEntity<GdnResponse<Boolean>> response1 = memberController.getValidateToken(token2,userEmail2);

        Assertions.assertTrue(response1.getBody().getData().booleanValue()==true);

    }

    @Test
    public void test_logout(){
        String token = "1234567890";
        String userEmail = "asd@gmail.com";

        LogoutResponseDTO logoutResponseDTO = new LogoutResponseDTO();
        logoutResponseDTO.setMessage("user logged out");

        Mockito.when(userRegisterService.logout(userEmail,token)).thenReturn(logoutResponseDTO);

        ResponseEntity<GdnResponse<LogoutResponseDTO>> response = memberController.logout(userEmail,token);

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.getBody().getData().getMessage().contains("user logged out"));

    }
}
