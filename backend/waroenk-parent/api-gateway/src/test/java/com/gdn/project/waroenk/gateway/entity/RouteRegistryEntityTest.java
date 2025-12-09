package com.gdn.project.waroenk.gateway.entity;

import com.gdn.project.waroenk.gateway.fixture.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RouteRegistryEntity Unit Tests")
class RouteRegistryEntityTest {

  @Nested
  @DisplayName("prePersist Tests")
  class PrePersistTests {

    @Test
    @DisplayName("Should generate UUID if not set")
    void shouldGenerateUuidIfNotSet() {
      // Given
      RouteRegistryEntity route = RouteRegistryEntity.builder()
          .httpMethod("GET")
          .path("/api/test")
          .grpcService("TestService")
          .grpcMethod("TestMethod")
          .build();

      // When
      route.prePersist();

      // Then
      assertThat(route.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should not override existing UUID")
    void shouldNotOverrideExistingUuid() {
      // Given
      UUID existingId = UUID.randomUUID();
      RouteRegistryEntity route = RouteRegistryEntity.builder()
          .id(existingId)
          .httpMethod("GET")
          .path("/api/test")
          .grpcService("TestService")
          .grpcMethod("TestMethod")
          .build();

      // When
      route.prePersist();

      // Then
      assertThat(route.getId()).isEqualTo(existingId);
    }
  }

  @Nested
  @DisplayName("getRequiredRolesList Tests")
  class GetRequiredRolesListTests {

    @Test
    @DisplayName("Should return roles as list")
    void shouldReturnRolesAsList() {
      // Given
      RouteRegistryEntity route = TestDataFactory.createRoute();
      route.setRequiredRoles(new String[]{"ADMIN", "USER"});

      // When
      List<String> roles = route.getRequiredRolesList();

      // Then
      assertThat(roles).containsExactly("ADMIN", "USER");
    }

    @Test
    @DisplayName("Should return empty list when roles is null")
    void shouldReturnEmptyListWhenNull() {
      // Given
      RouteRegistryEntity route = TestDataFactory.createRoute();
      route.setRequiredRoles(null);

      // When
      List<String> roles = route.getRequiredRolesList();

      // Then
      assertThat(roles).isEmpty();
    }
  }

  @Nested
  @DisplayName("setRequiredRolesList Tests")
  class SetRequiredRolesListTests {

    @Test
    @DisplayName("Should set roles from list")
    void shouldSetRolesFromList() {
      // Given
      RouteRegistryEntity route = TestDataFactory.createRoute();

      // When
      route.setRequiredRolesList(List.of("ADMIN", "MANAGER"));

      // Then
      assertThat(route.getRequiredRoles()).containsExactly("ADMIN", "MANAGER");
    }

    @Test
    @DisplayName("Should set null when list is null")
    void shouldSetNullWhenListIsNull() {
      // Given
      RouteRegistryEntity route = TestDataFactory.createRoute();
      route.setRequiredRoles(new String[]{"EXISTING"});

      // When
      route.setRequiredRolesList(null);

      // Then
      assertThat(route.getRequiredRoles()).isNull();
    }

    @Test
    @DisplayName("Should set null when list is empty")
    void shouldSetNullWhenListIsEmpty() {
      // Given
      RouteRegistryEntity route = TestDataFactory.createRoute();
      route.setRequiredRoles(new String[]{"EXISTING"});

      // When
      route.setRequiredRolesList(List.of());

      // Then
      assertThat(route.getRequiredRoles()).isNull();
    }
  }

  @Nested
  @DisplayName("Default Values Tests")
  class DefaultValuesTests {

    @Test
    @DisplayName("Should have default publicEndpoint as false")
    void shouldHaveDefaultPublicEndpoint() {
      // Given
      RouteRegistryEntity route = RouteRegistryEntity.builder()
          .httpMethod("GET")
          .path("/api/test")
          .grpcService("TestService")
          .grpcMethod("TestMethod")
          .build();

      // Then
      assertThat(route.isPublicEndpoint()).isFalse();
    }
  }

  @Nested
  @DisplayName("Route Builder Tests")
  class RouteBuilderTests {

    @Test
    @DisplayName("Should build route with all fields")
    void shouldBuildRouteWithAllFields() {
      // Given/When
      UUID id = UUID.randomUUID();
      RouteRegistryEntity route = RouteRegistryEntity.builder()
          .id(id)
          .httpMethod("POST")
          .path("/api/users/register")
          .grpcService("com.gdn.member.UserService")
          .grpcMethod("Register")
          .requestType("RegisterRequest")
          .responseType("RegisterResponse")
          .publicEndpoint(true)
          .routeHash("somehash123")
          .build();

      // Then
      assertThat(route.getId()).isEqualTo(id);
      assertThat(route.getHttpMethod()).isEqualTo("POST");
      assertThat(route.getPath()).isEqualTo("/api/users/register");
      assertThat(route.getGrpcService()).isEqualTo("com.gdn.member.UserService");
      assertThat(route.getGrpcMethod()).isEqualTo("Register");
      assertThat(route.getRequestType()).isEqualTo("RegisterRequest");
      assertThat(route.getResponseType()).isEqualTo("RegisterResponse");
      assertThat(route.isPublicEndpoint()).isTrue();
      assertThat(route.getRouteHash()).isEqualTo("somehash123");
    }
  }
}

