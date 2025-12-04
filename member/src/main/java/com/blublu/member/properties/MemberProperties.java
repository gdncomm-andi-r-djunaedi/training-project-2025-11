package com.blublu.member.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Data
@Component()
@ConfigurationProperties(prefix = "member")
public class MemberProperties {
  private HashMap<String, String> flag;
}
