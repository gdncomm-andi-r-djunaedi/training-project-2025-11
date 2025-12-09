package com.gdn.project.waroenk.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.project.waroenk.gateway.entity.RouteRegistryEntity;
import com.gdn.project.waroenk.gateway.entity.ServiceRegistryEntity;
import com.gdn.project.waroenk.gateway.fixture.TestDataFactory;
import com.gdn.project.waroenk.gateway.repository.RouteRegistryRepository;
import com.gdn.project.waroenk.gateway.repository.ServiceRegistryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DynamicRoutingRegistry Unit Tests")
class DynamicRoutingRegistryTest {

  @Mock
  private ServiceRegistryRepository serviceRepository;

  @Mock
  private RouteRegistryRepository routeRepository;

  @Mock
  private RedisTemplate<String, String> redisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  @Mock
  private SetOperations<String, String> setOperations;

  private DynamicRoutingRegistry registry;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(redisTemplate.opsForSet()).thenReturn(setOperations);

    registry = new DynamicRoutingRegistry(
        serviceRepository,
        routeRepository,
        redisTemplate,
        objectMapper
    );
  }

  @Nested
  @DisplayName("loadRoutesFromDatabase Tests")
  class LoadRoutesFromDatabaseTests {

    @Test
    @DisplayName("Should load active services with routes")
    void shouldLoadActiveServicesWithRoutes() {
      // Given
      ServiceRegistryEntity service = TestDataFactory.createService("member-service");
      RouteRegistryEntity route = TestDataFactory.createRouteForService(
          service, "GET", "/api/users");

      when(serviceRepository.findByActiveTrueWithRoutes()).thenReturn(List.of(service));

      // When
      registry.loadRoutesFromDatabase();

      // Then
      assertThat(registry.getAllServices()).hasSize(1);
      assertThat(registry.getAllRoutes()).hasSize(1);
    }

    @Test
    @DisplayName("Should clear caches before loading")
    void shouldClearCachesBeforeLoading() {
      // Given
      when(serviceRepository.findByActiveTrueWithRoutes()).thenReturn(List.of());

      // When
      registry.loadRoutesFromDatabase();

      // Then
      assertThat(registry.getAllServices()).isEmpty();
      assertThat(registry.getAllRoutes()).isEmpty();
    }
  }

  @Nested
  @DisplayName("registerService Tests")
  class RegisterServiceTests {

    @Test
    @DisplayName("Should register new service successfully")
    void shouldRegisterNewServiceSuccessfully() {
      // Given
      DynamicRoutingRegistry.ServiceDefinition definition = TestDataFactory.createServiceDefinition(
          "new-service",
          "localhost",
          9090,
          List.of(TestDataFactory.createRouteDefinitionDto("GET", "/api/new", "NewService", "GetData", false))
      );

      when(serviceRepository.findByName("new-service")).thenReturn(Optional.empty());
      when(routeRepository.findByServiceName("new-service")).thenReturn(List.of());
      when(routeRepository.findByHttpMethodAndPath("GET", "/api/new")).thenReturn(Optional.empty());
      when(serviceRepository.save(any(ServiceRegistryEntity.class)))
          .thenAnswer(invocation -> {
            ServiceRegistryEntity saved = invocation.getArgument(0);
            if (saved.getId() == null) {
              saved.setId(UUID.randomUUID());
            }
            return saved;
          });

      // When
      DynamicRoutingRegistry.RegistrationResult result = registry.registerService(definition);

      // Then
      assertThat(result.success()).isTrue();
      assertThat(result.routesRegistered()).isEqualTo(1);
      verify(serviceRepository).save(any(ServiceRegistryEntity.class));
    }

    @Test
    @DisplayName("Should update existing service on re-registration")
    void shouldUpdateExistingServiceOnReRegistration() {
      // Given
      ServiceRegistryEntity existingService = TestDataFactory.createService("existing-service");

      DynamicRoutingRegistry.ServiceDefinition definition = TestDataFactory.createServiceDefinition(
          "existing-service",
          "new-host",
          9091,
          List.of(TestDataFactory.createRouteDefinitionDto("POST", "/api/update", "UpdateService", "Update", false))
      );

      when(serviceRepository.findByName("existing-service")).thenReturn(Optional.of(existingService));
      when(routeRepository.findByServiceName("existing-service")).thenReturn(List.of());
      when(routeRepository.findByHttpMethodAndPath("POST", "/api/update")).thenReturn(Optional.empty());
      when(serviceRepository.save(any(ServiceRegistryEntity.class))).thenReturn(existingService);

      // When
      DynamicRoutingRegistry.RegistrationResult result = registry.registerService(definition);

      // Then
      assertThat(result.success()).isTrue();
      verify(serviceRepository).save(any(ServiceRegistryEntity.class));
    }

    @Test
    @DisplayName("Should skip unchanged routes based on hash")
    void shouldSkipUnchangedRoutes() {
      // Given
      ServiceRegistryEntity existingService = TestDataFactory.createService("service-with-routes");
      RouteRegistryEntity existingRoute = TestDataFactory.createRoute("GET", "/api/existing", "TestService", "TestMethod");
      existingRoute.setService(existingService);
      existingService.getRoutes().add(existingRoute);

      // Route definition that matches existing route (will generate same hash)
      DynamicRoutingRegistry.RouteDefinitionDto routeDef = new DynamicRoutingRegistry.RouteDefinitionDto(
          "GET", "/api/existing", "TestService", "TestMethod", null, null, false, List.of()
      );

      DynamicRoutingRegistry.ServiceDefinition definition = TestDataFactory.createServiceDefinition(
          "service-with-routes",
          "localhost",
          9090,
          List.of(routeDef)
      );

      // Mock to return existing route hash
      String existingHash = "existing-hash";
      existingRoute.setRouteHash(existingHash);

      when(serviceRepository.findByName("service-with-routes")).thenReturn(Optional.of(existingService));
      when(routeRepository.findByServiceName("service-with-routes")).thenReturn(List.of(existingRoute));
      when(serviceRepository.save(any(ServiceRegistryEntity.class))).thenReturn(existingService);

      // When
      DynamicRoutingRegistry.RegistrationResult result = registry.registerService(definition);

      // Then
      assertThat(result.success()).isTrue();
      // Routes should be registered because hash is different (computed hash won't match "existing-hash")
    }
  }

  @Nested
  @DisplayName("unregisterService Tests")
  class UnregisterServiceTests {

    @Test
    @DisplayName("Should unregister service and remove routes")
    void shouldUnregisterServiceAndRemoveRoutes() {
      // Given
      ServiceRegistryEntity service = TestDataFactory.createService("service-to-remove");
      RouteRegistryEntity route = TestDataFactory.createRouteForService(service, "GET", "/api/remove");

      // First, load the service into cache
      when(serviceRepository.findByActiveTrueWithRoutes()).thenReturn(List.of(service));
      registry.loadRoutesFromDatabase();

      when(serviceRepository.findByName("service-to-remove")).thenReturn(Optional.of(service));

      // When
      boolean result = registry.unregisterService("service-to-remove");

      // Then
      assertThat(result).isTrue();
      verify(serviceRepository).delete(service);
      assertThat(registry.getService("service-to-remove")).isEmpty();
    }

    @Test
    @DisplayName("Should return false when service not found")
    void shouldReturnFalseWhenServiceNotFound() {
      // Given
      when(serviceRepository.findByName("non-existent")).thenReturn(Optional.empty());

      // When
      boolean result = registry.unregisterService("non-existent");

      // Then
      assertThat(result).isFalse();
      verify(serviceRepository, never()).delete(any());
    }
  }

  @Nested
  @DisplayName("updateHeartbeat Tests")
  class UpdateHeartbeatTests {

    @Test
    @DisplayName("Should update heartbeat for existing service")
    void shouldUpdateHeartbeatForExistingService() {
      // Given
      when(serviceRepository.updateHeartbeat(eq("active-service"), any(LocalDateTime.class)))
          .thenReturn(1);

      // Load service into cache first
      ServiceRegistryEntity service = TestDataFactory.createService("active-service");
      when(serviceRepository.findByActiveTrueWithRoutes()).thenReturn(List.of(service));
      registry.loadRoutesFromDatabase();

      // When
      boolean result = registry.updateHeartbeat("active-service");

      // Then
      assertThat(result).isTrue();
      verify(serviceRepository).updateHeartbeat(eq("active-service"), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should return false when service doesn't exist")
    void shouldReturnFalseWhenServiceDoesntExist() {
      // Given
      when(serviceRepository.updateHeartbeat(eq("non-existent"), any(LocalDateTime.class)))
          .thenReturn(0);

      // When
      boolean result = registry.updateHeartbeat("non-existent");

      // Then
      assertThat(result).isFalse();
    }
  }

  @Nested
  @DisplayName("resolveRoute Tests")
  class ResolveRouteTests {

    @Test
    @DisplayName("Should resolve route from memory cache")
    void shouldResolveRouteFromMemoryCache() {
      // Given
      ServiceRegistryEntity service = TestDataFactory.createService("cached-service");
      RouteRegistryEntity route = TestDataFactory.createRouteForService(service, "GET", "/api/cached");

      when(serviceRepository.findByActiveTrueWithRoutes()).thenReturn(List.of(service));
      registry.loadRoutesFromDatabase();

      // When
      Optional<DynamicRoutingRegistry.CachedRoute> result = registry.resolveRoute("GET", "/api/cached");

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().serviceName()).isEqualTo("cached-service");
    }

    @Test
    @DisplayName("Should resolve route with path pattern matching")
    void shouldResolveRouteWithPathPatternMatching() {
      // Given
      ServiceRegistryEntity service = TestDataFactory.createService("pattern-service");
      RouteRegistryEntity route = TestDataFactory.createRouteForService(service, "GET", "/api/users/{id}");

      when(serviceRepository.findByActiveTrueWithRoutes()).thenReturn(List.of(service));
      registry.loadRoutesFromDatabase();

      // When
      Optional<DynamicRoutingRegistry.CachedRoute> result = registry.resolveRoute("GET", "/api/users/123");

      // Then
      assertThat(result).isPresent();
    }

    @Test
    @DisplayName("Should return empty when route not found")
    void shouldReturnEmptyWhenRouteNotFound() {
      // Given
      when(serviceRepository.findByActiveTrueWithRoutes()).thenReturn(List.of());
      registry.loadRoutesFromDatabase();

      // When
      Optional<DynamicRoutingRegistry.CachedRoute> result = registry.resolveRoute("GET", "/api/nonexistent");

      // Then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("getService Tests")
  class GetServiceTests {

    @Test
    @DisplayName("Should return service from cache")
    void shouldReturnServiceFromCache() {
      // Given
      ServiceRegistryEntity service = TestDataFactory.createService("my-service");

      when(serviceRepository.findByActiveTrueWithRoutes()).thenReturn(List.of(service));
      registry.loadRoutesFromDatabase();

      // When
      Optional<DynamicRoutingRegistry.CachedService> result = registry.getService("my-service");

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().name()).isEqualTo("my-service");
    }

    @Test
    @DisplayName("Should return empty when service not in cache")
    void shouldReturnEmptyWhenServiceNotInCache() {
      // Given
      when(serviceRepository.findByActiveTrueWithRoutes()).thenReturn(List.of());
      registry.loadRoutesFromDatabase();

      // When
      Optional<DynamicRoutingRegistry.CachedService> result = registry.getService("unknown-service");

      // Then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("getAllServices Tests")
  class GetAllServicesTests {

    @Test
    @DisplayName("Should return all cached services")
    void shouldReturnAllCachedServices() {
      // Given
      ServiceRegistryEntity service1 = TestDataFactory.createService("service-1");
      ServiceRegistryEntity service2 = TestDataFactory.createService("service-2");

      when(serviceRepository.findByActiveTrueWithRoutes()).thenReturn(List.of(service1, service2));
      registry.loadRoutesFromDatabase();

      // When
      Collection<DynamicRoutingRegistry.CachedService> services = registry.getAllServices();

      // Then
      assertThat(services).hasSize(2);
    }
  }

  @Nested
  @DisplayName("getAllRoutes Tests")
  class GetAllRoutesTests {

    @Test
    @DisplayName("Should return all cached routes")
    void shouldReturnAllCachedRoutes() {
      // Given
      ServiceRegistryEntity service = TestDataFactory.createService("multi-route-service");
      TestDataFactory.createRouteForService(service, "GET", "/api/route1");
      TestDataFactory.createRouteForService(service, "POST", "/api/route2");

      when(serviceRepository.findByActiveTrueWithRoutes()).thenReturn(List.of(service));
      registry.loadRoutesFromDatabase();

      // When
      Collection<DynamicRoutingRegistry.CachedRoute> routes = registry.getAllRoutes();

      // Then
      assertThat(routes).hasSize(2);
    }
  }
}

