package com.gdn.project.waroenk.gateway.repository;

import com.gdn.project.waroenk.gateway.entity.RouteRegistryEntity;
import com.gdn.project.waroenk.gateway.entity.ServiceRegistryEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("RouteRegistryRepository Integration Tests")
class RouteRegistryRepositoryIntegrationTest {

  @Autowired
  private RouteRegistryRepository routeRepository;

  @Autowired
  private ServiceRegistryRepository serviceRepository;

  @Autowired
  private TestEntityManager entityManager;

  private ServiceRegistryEntity activeService;
  private ServiceRegistryEntity inactiveService;
  private RouteRegistryEntity testRoute;

  @BeforeEach
  void setUp() {
    // Create active service
    activeService = ServiceRegistryEntity.builder()
        .id(UUID.randomUUID())
        .name("active-service")
        .protocol("grpc")
        .host("localhost")
        .port(9090)
        .active(true)
        .lastHeartbeat(LocalDateTime.now())
        .build();
    activeService = entityManager.persistAndFlush(activeService);

    // Create inactive service
    inactiveService = ServiceRegistryEntity.builder()
        .id(UUID.randomUUID())
        .name("inactive-service")
        .protocol("grpc")
        .host("localhost")
        .port(9091)
        .active(false)
        .lastHeartbeat(LocalDateTime.now())
        .build();
    inactiveService = entityManager.persistAndFlush(inactiveService);

    // Create test route
    testRoute = RouteRegistryEntity.builder()
        .id(UUID.randomUUID())
        .httpMethod("GET")
        .path("/api/users")
        .grpcService("com.gdn.member.UserService")
        .grpcMethod("GetUsers")
        .requestType("GetUsersRequest")
        .responseType("GetUsersResponse")
        .publicEndpoint(false)
        .routeHash("abc123hash")
        .build();
    testRoute.setService(activeService);
    testRoute = entityManager.persistAndFlush(testRoute);

    entityManager.clear();
  }

  @Nested
  @DisplayName("findAllActiveRoutes Tests")
  class FindAllActiveRoutesTests {

    @Test
    @DisplayName("Should find routes from active services only")
    void shouldFindRoutesFromActiveServicesOnly() {
      // Given - create route for inactive service
      RouteRegistryEntity inactiveRoute = RouteRegistryEntity.builder()
          .id(UUID.randomUUID())
          .httpMethod("POST")
          .path("/api/inactive")
          .grpcService("InactiveService")
          .grpcMethod("InactiveMethod")
          .build();
      inactiveRoute.setService(inactiveService);
      entityManager.persistAndFlush(inactiveRoute);

      // When
      List<RouteRegistryEntity> result = routeRepository.findAllActiveRoutes();

      // Then
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getPath()).isEqualTo("/api/users");
    }
  }

  @Nested
  @DisplayName("findByHttpMethodAndPath Tests")
  class FindByHttpMethodAndPathTests {

    @Test
    @DisplayName("Should find route by method and path")
    void shouldFindRouteByMethodAndPath() {
      // When
      Optional<RouteRegistryEntity> result = routeRepository.findByHttpMethodAndPath("GET", "/api/users");

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().getGrpcMethod()).isEqualTo("GetUsers");
    }

    @Test
    @DisplayName("Should return empty when method doesn't match")
    void shouldReturnEmptyWhenMethodDoesntMatch() {
      // When
      Optional<RouteRegistryEntity> result = routeRepository.findByHttpMethodAndPath("POST", "/api/users");

      // Then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when path doesn't match")
    void shouldReturnEmptyWhenPathDoesntMatch() {
      // When
      Optional<RouteRegistryEntity> result = routeRepository.findByHttpMethodAndPath("GET", "/api/nonexistent");

      // Then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("findByServiceName Tests")
  class FindByServiceNameTests {

    @Test
    @DisplayName("Should find all routes for a service")
    void shouldFindAllRoutesForService() {
      // Given - add another route
      RouteRegistryEntity anotherRoute = RouteRegistryEntity.builder()
          .id(UUID.randomUUID())
          .httpMethod("POST")
          .path("/api/users/register")
          .grpcService("com.gdn.member.UserService")
          .grpcMethod("Register")
          .build();
      anotherRoute.setService(activeService);
      entityManager.persistAndFlush(anotherRoute);

      // When
      List<RouteRegistryEntity> result = routeRepository.findByServiceName("active-service");

      // Then
      assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should return empty for non-existent service")
    void shouldReturnEmptyForNonExistentService() {
      // When
      List<RouteRegistryEntity> result = routeRepository.findByServiceName("non-existent");

      // Then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("findRouteHashesByServiceName Tests")
  class FindRouteHashesByServiceNameTests {

    @Test
    @DisplayName("Should find all route hashes for a service")
    void shouldFindRouteHashes() {
      // Given - add route with hash
      RouteRegistryEntity hashedRoute = RouteRegistryEntity.builder()
          .id(UUID.randomUUID())
          .httpMethod("DELETE")
          .path("/api/users/{id}")
          .grpcService("com.gdn.member.UserService")
          .grpcMethod("DeleteUser")
          .routeHash("def456hash")
          .build();
      hashedRoute.setService(activeService);
      entityManager.persistAndFlush(hashedRoute);

      // When
      List<String> result = routeRepository.findRouteHashesByServiceName("active-service");

      // Then
      assertThat(result).containsExactlyInAnyOrder("abc123hash", "def456hash");
    }
  }

  @Nested
  @DisplayName("findByRouteHashIn Tests")
  class FindByRouteHashInTests {

    @Test
    @DisplayName("Should find routes by hash list")
    void shouldFindRoutesByHashList() {
      // When
      List<RouteRegistryEntity> result = routeRepository.findByRouteHashIn(List.of("abc123hash", "nonexistent"));

      // Then
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getRouteHash()).isEqualTo("abc123hash");
    }
  }

  @Nested
  @DisplayName("deleteByServiceId Tests")
  class DeleteByServiceIdTests {

    @Test
    @DisplayName("Should delete all routes for a service")
    void shouldDeleteRoutesByServiceId() {
      // Given
      UUID serviceId = activeService.getId();

      // When
      routeRepository.deleteByServiceId(serviceId);
      entityManager.flush();

      // Then
      List<RouteRegistryEntity> remaining = routeRepository.findByServiceName("active-service");
      assertThat(remaining).isEmpty();
    }
  }

  @Nested
  @DisplayName("deleteByServiceName Tests")
  class DeleteByServiceNameTests {

    @Test
    @DisplayName("Should delete all routes by service name")
    void shouldDeleteRoutesByServiceName() {
      // When
      routeRepository.deleteByServiceName("active-service");
      entityManager.flush();

      // Then
      List<RouteRegistryEntity> remaining = routeRepository.findByServiceName("active-service");
      assertThat(remaining).isEmpty();
    }
  }

  @Nested
  @DisplayName("existsByHttpMethodAndPath Tests")
  class ExistsByHttpMethodAndPathTests {

    @Test
    @DisplayName("Should return true when route exists")
    void shouldReturnTrueWhenExists() {
      // When
      boolean exists = routeRepository.existsByHttpMethodAndPath("GET", "/api/users");

      // Then
      assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when route doesn't exist")
    void shouldReturnFalseWhenNotExists() {
      // When
      boolean exists = routeRepository.existsByHttpMethodAndPath("DELETE", "/api/nonexistent");

      // Then
      assertThat(exists).isFalse();
    }
  }

  @Nested
  @DisplayName("countActiveRoutes Tests")
  class CountActiveRoutesTests {

    @Test
    @DisplayName("Should count only routes from active services")
    void shouldCountOnlyActiveRoutes() {
      // Given - route for inactive service
      RouteRegistryEntity inactiveRoute = RouteRegistryEntity.builder()
          .id(UUID.randomUUID())
          .httpMethod("POST")
          .path("/api/inactive/route")
          .grpcService("InactiveService")
          .grpcMethod("InactiveMethod")
          .build();
      inactiveRoute.setService(inactiveService);
      entityManager.persistAndFlush(inactiveRoute);

      // When
      long count = routeRepository.countActiveRoutes();

      // Then
      assertThat(count).isEqualTo(1); // Only the testRoute from activeService
    }
  }

  @Nested
  @DisplayName("Route with Required Roles Tests")
  class RouteWithRequiredRolesTests {

    @Test
    @DisplayName("Should save and retrieve route with required roles")
    void shouldSaveAndRetrieveRouteWithRoles() {
      // Given
      RouteRegistryEntity protectedRoute = RouteRegistryEntity.builder()
          .id(UUID.randomUUID())
          .httpMethod("POST")
          .path("/api/admin/settings")
          .grpcService("AdminService")
          .grpcMethod("UpdateSettings")
          .publicEndpoint(false)
          .build();
      protectedRoute.setRequiredRolesList(List.of("ADMIN", "SUPER_ADMIN"));
      protectedRoute.setService(activeService);
      protectedRoute = entityManager.persistAndFlush(protectedRoute);
      entityManager.clear();

      // When
      Optional<RouteRegistryEntity> result = routeRepository.findByHttpMethodAndPath("POST", "/api/admin/settings");

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().getRequiredRolesList()).containsExactly("ADMIN", "SUPER_ADMIN");
    }

    @Test
    @DisplayName("Should handle route with null required roles")
    void shouldHandleNullRequiredRoles() {
      // When
      Optional<RouteRegistryEntity> result = routeRepository.findByHttpMethodAndPath("GET", "/api/users");

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().getRequiredRolesList()).isEmpty();
    }
  }
}

