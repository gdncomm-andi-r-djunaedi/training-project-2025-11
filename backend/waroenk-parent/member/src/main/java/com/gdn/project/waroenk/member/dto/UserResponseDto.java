package com.gdn.project.waroenk.member.dto;

import com.gdn.project.waroenk.member.constant.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDto(UUID id, String fullName, LocalDate dob, String email, String phoneNumber, Gender gender,
                              AddressResponseDto defaultAddress, LocalDateTime createdAt, LocalDateTime updatedAt) {

}
