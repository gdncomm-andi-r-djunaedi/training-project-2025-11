package MemberService.MemberService.dto;

import lombok.Data;

@Data
public class RegisterDto {
    private String email;
    private String fullName;
    private String password;
}
