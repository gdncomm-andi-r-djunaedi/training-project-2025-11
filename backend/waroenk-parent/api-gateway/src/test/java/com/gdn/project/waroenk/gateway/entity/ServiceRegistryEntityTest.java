package com.gdn.project.waroenk.gateway.entity;

import com.gdn.project.waroenk.gateway.fixture.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ServiceRegistryEntity Unit Tests")
class ServiceRegistryEntityTest {

  @Nested
  @DisplayName("prePersist Tests")
  class PrePersistTests {

    @Test
    @DisplayName("Should generate UUID if not set")
    void shouldGenerateUuidIfNotSet() {
      // Given
      ServiceRegistryEntity service = ServiceRegistryEntity.builder()
          .name("test-service")
          .host("localhost")
          .port(9090)
          .build();

      // When
      service.prePersist();

      // Then
      assertThat(service.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should not override existing UUID")
    void shouldNotOverrideExistingUuid() {
      // Given
      UUID existingId = UUID.randomUUID();
      ServiceRegistryEntity service = ServiceRegistryEntity.builder()
          .id(existingId)
          .name("test-service")
          .host("localhost")
          .port(9090)
          .build();

      // When
      service.prePersist();

      // Then
      assertThat(service.getId()).isEqualTo(existingId);
    }
  }

  @Nested
  @DisplayName("addRoute Tests")
  class AddRouteTests {

    @Test
    @DisplayName("Should add route and set bidirectional relationship")
    void shouldAddRouteWithBidirectionalRelationship() {
      // Given
      ServiceRegistryEntity service = TestDataFactory.createService("test-service");
      RouteRegistryEntity route = TestDataFactory.createRoute("GET", "/api/test", "TestService", "TestMethod");

      // When
      service.addRoute(route);

      // Then
      assertThat(service.getRoutes()).contains(route);
      assertThat(route.getService()).isEqualTo(service);
    }

    @Test
    @DisplayName("Should add multiple routes")
    void shouldAddMultipleRoutes() {
      // Given
      ServiceRegistryEntity service = TestDataFactory.createService("test-service");
      RouteRegistryEntity route1 = TestDataFactory.createRoute("GET", "/api/test1", "TestService", "Method1");
      RouteRegistryEntity route2 = TestDataFactory.createRoute("POST", "/api/test2", "TestService", "Method2");

      // When
      service.addRoute(route1);
      service.addRoute(route2);

      // Then
      assertThat(service.getRoutes()).hasSize(2);
    }
  }

  @Nested
  @DisplayName("removeRoute Tests")
  class RemoveRouteTests {

    @Test
    @DisplayName("Should remove route and clear relationship")
    void shouldRemoveRouteAndClearRelationship() {
      // Given
      ServiceRegistryEntity service = TestDataFactory.createService("test-service");
      RouteRegistryEntity route = TestDataFactory.createRoute("GET", "/api/test", "TestService", "TestMethod");
      service.addRoute(route);

      // When
      service.removeRoute(route);

      // Then
      assertThat(service.getRoutes()).isEmpty();
      assertThat(route.getService()).isNull();
    }
  }

  @Nested
  @DisplayName("Default Values Tests")
  class DefaultValuesTests {

    @Test
    @DisplayName("Should have default protocol as grpc")
    void shouldHaveDefaultProtocol() {
      // Given
      ServiceRegistryEntity service = ServiceRegistryEntity.builder()
          .name("test")
          .host("localhost")
          .port(9090)
          .build();

      // Then
      assertThat(service.getProtocol()).isEqualTo("grpc");
    }

    @Test
    @DisplayName("Should have default useTls as false")
    void shouldHaveDefaultUseTls() {
      // Given
      ServiceRegistryEntity service = ServiceRegistryEntity.builder()
          .name("test")
          .host("localhost")
          .port(9090)
          .build();

      // Then
      assertThat(service.isUseTls()).isFalse();
    }

    @Test
    @DisplayName("Should have default active as true")
    void shouldHaveDefaultActive() {
      // Given
      ServiceRegistryEntity service = ServiceRegistryEntity.builder()
          .name("test")
          .host("localhost")
          .port(9090)
          .build();

      // Then
      assertThat(service.isActive()).isTrue();
    }
  }
}

