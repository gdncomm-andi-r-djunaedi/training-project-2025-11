package com.gdn.project.waroenk.member.dto;

import java.time.LocalDateTime;

public record SystemParameterResponseDto(String id, String variable, String data, String description,
                                         LocalDateTime createdAt, LocalDateTime updatedAt) {

}
