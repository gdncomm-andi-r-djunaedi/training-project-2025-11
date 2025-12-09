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
@DisplayName("ServiceRegistryRepository Integration Tests")
class ServiceRegistryRepositoryIntegrationTest {

  @Autowired
  private ServiceRegistryRepository serviceRepository;

  @Autowired
  private RouteRegistryRepository routeRepository;

  @Autowired
  private TestEntityManager entityManager;

  private ServiceRegistryEntity testService;

  @BeforeEach
  void setUp() {
    testService = ServiceRegistryEntity.builder()
        .id(UUID.randomUUID())
        .name("member-service")
        .protocol("grpc")
        .host("localhost")
        .port(9090)
        .useTls(false)
        .version("1.0.0")
        .active(true)
        .lastHeartbeat(LocalDateTime.now())
        .build();
    testService = entityManager.persistAndFlush(testService);
    entityManager.clear();
  }

  @Nested
  @DisplayName("findByName Tests")
  class FindByNameTests {

    @Test
    @DisplayName("Should find service by name")
    void shouldFindServiceByName() {
      // When
      Optional<ServiceRegistryEntity> result = serviceRepository.findByName("member-service");

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().getName()).isEqualTo("member-service");
      assertThat(result.get().getHost()).isEqualTo("localhost");
    }

    @Test
    @DisplayName("Should return empty when name not found")
    void shouldReturnEmptyWhenNameNotFound() {
      // When
      Optional<ServiceRegistryEntity> result = serviceRepository.findByName("non-existent");

      // Then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("findByActiveTrue Tests")
  class FindByActiveTrueTests {

    @Test
    @DisplayName("Should find only active services")
    void shouldFindOnlyActiveServices() {
      // Given
      ServiceRegistryEntity inactiveService = ServiceRegistryEntity.builder()
          .id(UUID.randomUUID())
          .name("inactive-service")
          .protocol("grpc")
          .host("localhost")
          .port(9091)
          .active(false)
          .lastHeartbeat(LocalDateTime.now())
          .build();
      entityManager.persistAndFlush(inactiveService);

      // When
      List<ServiceRegistryEntity> result = serviceRepository.findByActiveTrue();

      // Then
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getName()).isEqualTo("member-service");
    }
  }

  @Nested
  @DisplayName("findByActiveTrueWithRoutes Tests")
  class FindByActiveTrueWithRoutesTests {

    @Test
    @DisplayName("Should find active services with routes eagerly loaded")
    void shouldFindActiveServicesWithRoutes() {
      // Given
      RouteRegistryEntity route = RouteRegistryEntity.builder()
          .id(UUID.randomUUID())
          .httpMethod("GET")
          .path("/api/users")
          .grpcService("UserService")
          .grpcMethod("GetUsers")
          .publicEndpoint(false)
          .build();
      route.setService(testService);
      entityManager.persistAndFlush(route);
      entityManager.clear();

      // When
      List<ServiceRegistryEntity> result = serviceRepository.findByActiveTrueWithRoutes();

      // Then
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getRoutes()).hasSize(1);
    }
  }

  @Nested
  @DisplayName("findStaleServices Tests")
  class FindStaleServicesTests {

    @Test
    @DisplayName("Should find services with old heartbeat")
    void shouldFindStaleServices() {
      // Given
      ServiceRegistryEntity staleService = ServiceRegistryEntity.builder()
          .id(UUID.randomUUID())
          .name("stale-service")
          .protocol("grpc")
          .host("localhost")
          .port(9092)
          .active(true)
          .lastHeartbeat(LocalDateTime.now().minusMinutes(10))
          .build();
      entityManager.persistAndFlush(staleService);

      LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);

      // When
      List<ServiceRegistryEntity> result = serviceRepository.findStaleServices(threshold);

      // Then
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getName()).isEqualTo("stale-service");
    }
  }

  @Nested
  @DisplayName("updateHeartbeat Tests")
  class UpdateHeartbeatTests {

    @Test
    @DisplayName("Should update heartbeat timestamp")
    void shouldUpdateHeartbeat() {
      // Given
      LocalDateTime newTimestamp = LocalDateTime.now().plusMinutes(1);

      // When
      int updated = serviceRepository.updateHeartbeat("member-service", newTimestamp);
      entityManager.flush();
      entityManager.clear();

      // Then
      assertThat(updated).isEqualTo(1);
      Optional<ServiceRegistryEntity> service = serviceRepository.findByName("member-service");
      assertThat(service).isPresent();
      assertThat(service.get().getLastHeartbeat()).isEqualToIgnoringNanos(newTimestamp);
    }

    @Test
    @DisplayName("Should return 0 when service not found")
    void shouldReturnZeroWhenServiceNotFound() {
      // When
      int updated = serviceRepository.updateHeartbeat("non-existent", LocalDateTime.now());

      // Then
      assertThat(updated).isZero();
    }
  }

  @Nested
  @DisplayName("deactivateService Tests")
  class DeactivateServiceTests {

    @Test
    @DisplayName("Should deactivate service")
    void shouldDeactivateService() {
      // When
      int deactivated = serviceRepository.deactivateService("member-service");
      entityManager.flush();
      entityManager.clear();

      // Then
      assertThat(deactivated).isEqualTo(1);
      Optional<ServiceRegistryEntity> service = serviceRepository.findByName("member-service");
      assertThat(service).isPresent();
      assertThat(service.get().isActive()).isFalse();
    }
  }

  @Nested
  @DisplayName("deactivateStaleServices Tests")
  class DeactivateStaleServicesTests {

    @Test
    @DisplayName("Should deactivate all stale services")
    void shouldDeactivateStaleServices() {
      // Given
      ServiceRegistryEntity staleService1 = ServiceRegistryEntity.builder()
          .id(UUID.randomUUID())
          .name("stale-1")
          .protocol("grpc")
          .host("localhost")
          .port(9093)
          .active(true)
          .lastHeartbeat(LocalDateTime.now().minusMinutes(10))
          .build();
      ServiceRegistryEntity staleService2 = ServiceRegistryEntity.builder()
          .id(UUID.randomUUID())
          .name("stale-2")
          .protocol("grpc")
          .host("localhost")
          .port(9094)
          .active(true)
          .lastHeartbeat(LocalDateTime.now().minusMinutes(15))
          .build();
      entityManager.persistAndFlush(staleService1);
      entityManager.persistAndFlush(staleService2);

      LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);

      // When
      int deactivated = serviceRepository.deactivateStaleServices(threshold);
      entityManager.flush();
      entityManager.clear();

      // Then
      assertThat(deactivated).isEqualTo(2);
      List<ServiceRegistryEntity> activeServices = serviceRepository.findByActiveTrue();
      assertThat(activeServices).hasSize(1); // Only testService remains active
    }
  }

  @Nested
  @DisplayName("existsByName Tests")
  class ExistsByNameTests {

    @Test
    @DisplayName("Should return true when service exists")
    void shouldReturnTrueWhenExists() {
      // When
      boolean exists = serviceRepository.existsByName("member-service");

      // Then
      assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when service doesn't exist")
    void shouldReturnFalseWhenNotExists() {
      // When
      boolean exists = serviceRepository.existsByName("non-existent");

      // Then
      assertThat(exists).isFalse();
    }
  }
}

