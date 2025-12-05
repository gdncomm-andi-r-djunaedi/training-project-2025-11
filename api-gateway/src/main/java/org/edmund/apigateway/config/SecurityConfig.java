package org.edmund.apigateway.config;
import org.edmund.apigateway.properties.GatewaySecurityProperties;
import org.edmund.apigateway.properties.JwtProperties;
import org.edmund.apigateway.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.edmund.commonlibrary.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final GatewaySecurityProperties securityProps;
  private final JwtProperties jwtProps;

  @Bean
  public JwtService jwtService() {
    return new JwtService(jwtProps.getSecret());
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> {

          if (securityProps.getPublicPaths() != null) {
            securityProps.getPublicPaths().forEach(path -> auth.requestMatchers(path).permitAll());
          }

          if (securityProps.getAuthenticatedPaths() != null) {
            securityProps.getAuthenticatedPaths().forEach(path -> auth.requestMatchers(path).authenticated());
          }

          auth.anyRequest().permitAll();
        })
        .addFilterBefore(new JwtAuthFilter(jwtService, securityProps), UsernamePasswordAuthenticationFilter.class)
        .httpBasic(Customizer.withDefaults());

    return http.build();
  }
}