package com.gdn.member.service;

import com.gdn.member.dto.request.LoginDTO;
import com.gdn.member.dto.response.LoginResponseDTO;
import com.gdn.member.dto.request.MemberRegisterDTO;
import org.springframework.http.HttpStatus;

public interface MemberService {
    public HttpStatus saveDetails(MemberRegisterDTO memberRegisterDTO);
    public LoginResponseDTO login(LoginDTO loginDTO);
    public String findPasswordByEmail(String email);
    public HttpStatus logout(String email);

}
