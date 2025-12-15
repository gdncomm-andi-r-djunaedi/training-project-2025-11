package com.yuna.auth.client;

import com.yuna.auth.dto.LoginUserDto;
import com.yuna.auth.request.CreateMemberRequest;
import com.yuna.auth.response.MemberResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MemberClient {

  private RestClient restClient;

  @Value("${member.client.host}")
  private String memberHost;

  @PostConstruct
  public void init () {
    this.restClient = RestClient.builder()
        .baseUrl(memberHost)
        .build();
  }

  public MemberResponse getMember(String username) {
    ResponseEntity<MemberResponse> response = restClient.get()
        .uri("/api/users/{username}", username)
        .retrieve()
        .toEntity(MemberResponse.class); // Execute GET request and map response to String

    if (response.getStatusCode().is2xxSuccessful()) {
      return response.getBody();
    } else {
      // Handle error cases
      throw new RuntimeException("Failed to fetch data: " + response.getStatusCode());
    }
  }

  public MemberResponse getMemberByUsernameAndPassword(LoginUserDto loginUserDto) {
    ResponseEntity<MemberResponse> response = restClient.get()
        .uri("/api/users/{username}?password={p1}", loginUserDto.getUsername(), loginUserDto.getPassword())
        .retrieve()
        .toEntity(MemberResponse.class); // Execute GET request and map response to String

    if (response.getStatusCode().is2xxSuccessful()) {
      return response.getBody();
    } else {
      // Handle error cases
      throw new RuntimeException("Failed to fetch data: " + response.getStatusCode());
    }
  }

  public MemberResponse createMember(CreateMemberRequest request) {
    ResponseEntity<MemberResponse> response = restClient.post()
        .uri("/api/users")
        .body(request)
        .retrieve()
        .toEntity(MemberResponse.class); // Execute GET request and map response to String

    if (response.getStatusCode().is2xxSuccessful()) {
      return response.getBody();
    } else {
      // Handle error cases
      throw new RuntimeException("Failed to fetch data: " + response.getStatusCode());
    }
  }
}
