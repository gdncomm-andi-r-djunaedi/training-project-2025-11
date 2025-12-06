package MemberService.MemberService.service;

import MemberService.MemberService.dto.LoginDto;
import MemberService.MemberService.dto.RegisterDto;
import MemberService.MemberService.entity.Member;


public interface MemberService {
    Member register(RegisterDto registerDto);

    String login(LoginDto loginDto);

    Member getProfile(String userId);

    String bulkRegister(int count);
}
