package com.blibli.product.config;

import com.blibli.product.enums.CategoryType;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(@NonNull FormatterRegistry registry) {
        registry.addConverter(new StringToCategoryTypeConverter());
    }

    public static class StringToCategoryTypeConverter implements Converter<String, CategoryType> {
        @Override
        public CategoryType convert(@NonNull String source) {
            return CategoryType.fromValue(source);
        }
    }
}

