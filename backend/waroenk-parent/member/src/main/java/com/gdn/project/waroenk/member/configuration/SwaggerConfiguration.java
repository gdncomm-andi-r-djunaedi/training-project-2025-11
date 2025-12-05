package com.gdn.project.waroenk.member.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
    title = "Member Application for Waroenk",
    description = "Member Application for Waroenk",
    contact = @Contact(
        name = "Yunaz Ramadhan",
        email = "yunaz.ramadhan@gdn-commerce.com"
    ),
    version = "1.0.0"
))
public class SwaggerConfiguration {
}
