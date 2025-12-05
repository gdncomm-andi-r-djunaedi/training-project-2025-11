package com.gdn.project.waroenk.member.dto;

import java.util.List;

public record ListOfAddressResponseDto(
    List<AddressResponseDto> data,
    String nextToken,
    Integer total) {
}







