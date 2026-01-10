package com.blibli.memberModule.services;

import com.blibli.memberModule.dto.CheckUserResponse;
import com.blibli.memberModule.dto.Memberdto;

public interface MemberService {
    Memberdto registerMember(Memberdto memberdto);
    CheckUserResponse validateMember(String email, String password);
    Memberdto getMemberByEmail(String email);
}
