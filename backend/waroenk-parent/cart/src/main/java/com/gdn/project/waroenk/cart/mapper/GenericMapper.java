package com.gdn.project.waroenk.cart.mapper;

import com.gdn.project.waroenk.common.Basic;
import com.gdn.project.waroenk.common.SortBy;
import com.gdn.project.waroenk.cart.dto.BasicDto;
import com.gdn.project.waroenk.cart.dto.SortByDto;
import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

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

    default Timestamp toTimestamp(Instant instant) {
        if (instant == null) {
            return null;
        }
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    default Instant toInstant(Timestamp timestamp) {
        if (timestamp == null || (timestamp.getSeconds() == 0 && timestamp.getNanos() == 0)) {
            return null;
        }
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}




