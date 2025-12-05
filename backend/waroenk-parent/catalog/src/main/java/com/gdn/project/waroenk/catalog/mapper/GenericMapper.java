package com.gdn.project.waroenk.catalog.mapper;

import com.gdn.project.waroenk.common.Basic;
import com.gdn.project.waroenk.common.SortBy;
import com.gdn.project.waroenk.catalog.dto.BasicDto;
import com.gdn.project.waroenk.catalog.dto.SortByDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GenericMapper {

  GenericMapper INSTANCE = Mappers.getMapper(GenericMapper.class);

  default SortBy toSortByGrpc(SortByDto sortByDto) {
    if (sortByDto == null) {
      return null;
    }
    return SortBy.newBuilder()
        .setField(sortByDto.field() != null ? sortByDto.field() : "id")
        .setDirection(sortByDto.direction() != null ? sortByDto.direction() : "asc")
        .build();
  }

  SortByDto toSortByDto(SortBy sortByGrpc);

  BasicDto toBasicDto(Basic basic);
}






