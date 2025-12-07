package com.marketplace.common.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Generic mapper service using Jackson ObjectMapper.
 */
@Component
@RequiredArgsConstructor
public class MapperService {

    private final ObjectMapper objectMapper;

    /**
     * Map source object to target class.
     *
     * @param source      Source object
     * @param targetClass Target class type
     * @return Mapped object
     */
    public <T> T map(Object source, Class<T> targetClass) {
        return objectMapper.convertValue(source, targetClass);
    }
}
