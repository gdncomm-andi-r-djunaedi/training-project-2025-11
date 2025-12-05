package com.gdn.project.waroenk.cart.dto.systemparameter;

import java.util.List;

public record ListOfSystemParameterResponseDto(
    List<SystemParameterResponseDto> data,
    String nextToken,
    Integer total
) {}




