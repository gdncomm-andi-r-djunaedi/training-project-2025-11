package com.gdn.project.waroenk.catalog.dto.merchant;

public record UpdateMerchantRequestDto(
    String name,
    String code,
    String iconUrl,
    String location,
    ContactInfoDto contact,
    Float rating
) {
  public record ContactInfoDto(String phone, String email) {}
}
