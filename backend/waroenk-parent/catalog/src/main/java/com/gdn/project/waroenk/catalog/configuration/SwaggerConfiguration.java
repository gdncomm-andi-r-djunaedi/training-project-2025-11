package com.gdn.project.waroenk.catalog.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
    title = "Catalog Application for Waroenk",
    description = "Catalog Application for Waroenk",
    contact = @Contact(
        name = "Yunaz Ramadhan",
        email = "yunaz.ramadhan@gdn-commerce.com"
    ),
    version = "1.0.0"
))
public class SwaggerConfiguration {
}
