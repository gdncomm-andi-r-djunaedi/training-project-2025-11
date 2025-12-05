package com.gdn.project.waroenk.member.dto;

import java.util.List;

public record ListOfUserResponseDto(
    List<UserResponseDto> data,
    String nextToken,
    Integer total) {
}







