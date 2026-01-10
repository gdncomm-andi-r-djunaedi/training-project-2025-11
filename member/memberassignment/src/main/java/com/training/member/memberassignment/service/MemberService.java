package com.training.member.memberassignment.service;

import com.training.member.memberassignment.dto.InputDTO;
import com.training.member.memberassignment.dto.OutputDTO;

public interface MemberService {
    void register(InputDTO inputDTO);

    OutputDTO login(InputDTO inputDTO);

}
