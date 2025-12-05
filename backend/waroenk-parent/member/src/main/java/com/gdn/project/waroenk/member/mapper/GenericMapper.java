package com.gdn.project.waroenk.member.mapper;

import com.gdn.project.waroenk.common.Basic;
import com.gdn.project.waroenk.common.SortBy;
import com.gdn.project.waroenk.member.dto.BasicDto;
import com.gdn.project.waroenk.member.dto.SortByDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GenericMapper {

  GenericMapper INSTANCE = Mappers.getMapper(GenericMapper.class);

  default SortBy toSortByGrpc(SortByDto sortByDto) {
    return SortBy.newBuilder().setField(sortByDto.field()).setDirection(sortByDto.direction()).build();
  }

  SortByDto toSortByDto(SortBy sortByGrpc);

  BasicDto toBasicDto(Basic basic);

}
