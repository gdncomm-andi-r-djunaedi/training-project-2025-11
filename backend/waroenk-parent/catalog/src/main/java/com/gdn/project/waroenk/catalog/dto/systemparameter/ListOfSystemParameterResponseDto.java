package com.gdn.project.waroenk.catalog.dto.systemparameter;

import java.util.List;

public record ListOfSystemParameterResponseDto(
    List<SystemParameterResponseDto> data,
    String nextToken,
    Integer total
) {}






