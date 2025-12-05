package com.blublu.member.model;

import com.blublu.member.entity.Member;
import com.blublu.member.exception.UsernameExistException;
import com.blublu.member.exception.UsernameNotExistException;
import com.blublu.member.exception.WrongPasswordException;
import com.blublu.member.model.request.LoginRequest;
import com.blublu.member.model.request.SignUpRequest;
import com.blublu.member.model.response.GenericBodyResponse;
import com.blublu.member.model.response.LoginResponse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelTest {

  @Test
  void testMember() {
    Member member = new Member();
    member.setId(1L);
    member.setUsername("user");
    member.setPassword("pass");

    assertEquals(1L, member.getId());
    assertEquals("user", member.getUsername());
    assertEquals("pass", member.getPassword());

    Member builtMember = Member.builder().id(2L).username("user2").password("pass2").build();
    assertEquals(2L, builtMember.getId());
  }

  @Test
  void testLoginRequest() {
    LoginRequest request = LoginRequest.builder().username("u").password("p").build();
    assertEquals("u", request.getUsername());
    assertEquals("p", request.getPassword());

    request.setUsername("u2");
    request.setPassword("p2");
    assertEquals("u2", request.getUsername());
  }

  @Test
  void testSignUpRequest() {
    SignUpRequest request = SignUpRequest.builder().username("u").password("p").build();
    assertEquals("u", request.getUsername());
    assertEquals("p", request.getPassword());
  }

  @Test
  void testLoginResponse() {
    LoginResponse response = LoginResponse.builder().username("u").success(true).build();
    assertEquals("u", response.getUsername());
    assertTrue(response.getSuccess());
  }

  @Test
  void testGenericBodyResponse() {
    List<String> content = new ArrayList<>();
    GenericBodyResponse response = GenericBodyResponse.builder().success(true).content(content).build();
    assertTrue(response.isSuccess());
    assertEquals(content, response.getContent());
  }

  @Test
  void testExceptions() {
    UsernameExistException e1 = new UsernameExistException("msg1");
    assertEquals("msg1", e1.getMessage());

    UsernameNotExistException e2 = new UsernameNotExistException("msg2");
    assertEquals("msg2", e2.getMessage());

    WrongPasswordException e3 = new WrongPasswordException("msg3");
    assertEquals("msg3", e3.getMessage());
  }

  @Test
  void testMemberProperties() {
    com.blublu.member.properties.MemberProperties props = new com.blublu.member.properties.MemberProperties();
    java.util.HashMap<String, String> map = new java.util.HashMap<>();
    map.put("k", "v");
    props.setFlag(map);
    assertEquals(map, props.getFlag());
  }
}
