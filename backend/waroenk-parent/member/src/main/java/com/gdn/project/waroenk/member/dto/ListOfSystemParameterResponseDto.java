package com.gdn.project.waroenk.member.dto;

import java.util.List;

public record ListOfSystemParameterResponseDto(List<SystemParameterResponseDto> data, String nextToken, Integer total) {

}
