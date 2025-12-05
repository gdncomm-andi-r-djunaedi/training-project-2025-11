package com.gdn.project.waroenk.catalog.dto.merchant;

import jakarta.validation.constraints.NotBlank;

public record CreateMerchantRequestDto(
    @NotBlank(message = "Name is required") String name,
    @NotBlank(message = "Code is required") String code,
    String iconUrl,
    String location,
    ContactInfoDto contact,
    Float rating
) {
  public record ContactInfoDto(String phone, String email) {}
}
