package com.zasura.apiGateway.service;

import com.zasura.apiGateway.dto.MemberDetailResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ReactiveMemberDetailsService implements ReactiveUserDetailsService {

  private final WebClient memberServiceWebClient;

  public ReactiveMemberDetailsService(WebClient memberServiceWebClient) {
    this.memberServiceWebClient = memberServiceWebClient;
  }

  @Override
  public Mono<UserDetails> findByUsername(String username) {
    return memberServiceWebClient.get()
        .uri("/api/member/{memberId}", username)
        .retrieve()

        .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
          if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
            return Mono.error(new UsernameNotFoundException("Member not found: " + username));
          }
          return Mono.error(new RuntimeException("Error retrieving member details from service."));
        })
        .toEntity(MemberDetailResponse.class)
        .map(dto -> User.withUsername(String.valueOf(dto.getBody().getData().getId()))
            .password(dto.getBody().getData().getPassword())
            .roles("USER")
            .build());
  }
}
