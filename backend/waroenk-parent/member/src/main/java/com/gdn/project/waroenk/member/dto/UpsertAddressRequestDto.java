package com.gdn.project.waroenk.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpsertAddressRequestDto(
    @NotBlank(message = "User ID is required") String userId,
    @NotBlank(message = "Label is required") @Size(max = 125) String label,
    @NotBlank(message = "Country is required") @Size(max = 125) String country,
    @NotBlank(message = "Province is required") @Size(max = 125) String province,
    @NotBlank(message = "City is required") @Size(max = 125) String city,
    @NotBlank(message = "District is required") @Size(max = 125) String district,
    @NotBlank(message = "Subdistrict is required") @Size(max = 125) String subDistrict,
    @NotBlank(message = "Postal code is required") @Size(max = 50) String postalCode,
    @NotBlank(message = "Street is required") @Size(max = 255) String street,
    Float latitude,
    Float longitude,
    @Size(max = 255) String details) {
}







