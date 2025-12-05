package com.gdn.project.waroenk.cart.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
    title = "Cart Application for Waroenk",
    description = "Cart and Checkout Application for Waroenk E-commerce Platform",
    contact = @Contact(
        name = "Yunaz Ramadhan",
        email = "yunaz.ramadhan@gdn-commerce.com"
    ),
    version = "1.0.0"
))
public class SwaggerConfiguration {
}
