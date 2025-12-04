package com.blublu.product.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Data
@Component()
@ConfigurationProperties(prefix = "product")
public class ProductProperties {
  private HashMap<String, String> flag;
}
