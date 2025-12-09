package com.marketplace.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {
    
    private Long id;
    private String email;
    private String username;
    private String fullName;
    private LocalDateTime createdAt;
}
