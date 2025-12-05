package com.gdn.project.waroenk.member.mapper;

import com.gdn.project.waroenk.common.VersionResponse;
import com.gdn.project.waroenk.member.dto.VersionResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface VersionMapper {

  VersionMapper INSTANCE = Mappers.getMapper(VersionMapper.class);

  VersionResponseDto toResponseDto(VersionResponse grpc);
}
