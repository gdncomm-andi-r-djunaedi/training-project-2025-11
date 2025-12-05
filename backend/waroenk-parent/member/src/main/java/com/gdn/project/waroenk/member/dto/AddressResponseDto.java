package com.gdn.project.waroenk.member.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AddressResponseDto(UUID id, String label, Float longitude, Float latitude, String country,
                                 String postalCode, String province, String city, String district, String subdistrict,
                                 String street, String details, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
