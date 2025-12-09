package com.gdn.project.waroenk.gateway.fixture;

import com.gdn.project.waroenk.gateway.entity.RouteRegistryEntity;
import com.gdn.project.waroenk.gateway.entity.ServiceRegistryEntity;
import com.gdn.project.waroenk.gateway.service.DynamicRoutingRegistry;
import net.datafaker.Faker;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Factory class for creating test data using DataFaker.
 * Provides consistent mock data for unit and integration tests.
 */
public class TestDataFactory {

  private static final Faker faker = new Faker();

  // ============================================================
  // Service Registry Entity
  // ============================================================

  /**
   * Creates a ServiceRegistryEntity with random data.
   */
  public static ServiceRegistryEntity createService() {
    return ServiceRegistryEntity.builder()
        .id(UUID.randomUUID())
        .name("test-service-" + faker.number().digits(4))
        .protocol("grpc")
        .host("localhost")
        .port(faker.number().numberBetween(9000, 9999))
        .useTls(false)
        .version("1.0.0")
        .active(true)
        .lastHeartbeat(LocalDateTime.now())
        .routes(new ArrayList<>())
        .build();
  }

  /**
   * Creates a ServiceRegistryEntity with specific name.
   */
  public static ServiceRegistryEntity createService(String name) {
    ServiceRegistryEntity service = createService();
    service.setName(name);
    return service;
  }

  /**
   * Creates a ServiceRegistryEntity with specific name, host, and port.
   */
  public static ServiceRegistryEntity createService(String name, String host, int port) {
    return ServiceRegistryEntity.builder()
        .id(UUID.randomUUID())
        .name(name)
        .protocol("grpc")
        .host(host)
        .port(port)
        .useTls(false)
        .version("1.0.0")
        .active(true)
        .lastHeartbeat(LocalDateTime.now())
        .routes(new ArrayList<>())
        .build();
  }

  /**
   * Creates an inactive ServiceRegistryEntity.
   */
  public static ServiceRegistryEntity createInactiveService(String name) {
    ServiceRegistryEntity service = createService(name);
    service.setActive(false);
    return service;
  }

  /**
   * Creates a stale ServiceRegistryEntity (old heartbeat).
   */
  public static ServiceRegistryEntity createStaleService(String name) {
    ServiceRegistryEntity service = createService(name);
    service.setLastHeartbeat(LocalDateTime.now().minusMinutes(10));
    return service;
  }

  // ============================================================
  // Route Registry Entity
  // ============================================================

  /**
   * Creates a RouteRegistryEntity with random data.
   */
  public static RouteRegistryEntity createRoute() {
    return RouteRegistryEntity.builder()
        .id(UUID.randomUUID())
        .httpMethod("GET")
        .path("/api/" + faker.lorem().word() + "/" + faker.lorem().word())
        .grpcService("com.gdn.project.waroenk." + faker.lorem().word() + ".TestService")
        .grpcMethod(faker.lorem().word())
        .requestType("com.gdn.project.waroenk.TestRequest")
        .responseType("com.gdn.project.waroenk.TestResponse")
        .publicEndpoint(false)
        .routeHash(UUID.randomUUID().toString().replace("-", "").substring(0, 64))
        .build();
  }

  /**
   * Creates a RouteRegistryEntity with specific details.
   */
  public static RouteRegistryEntity createRoute(String httpMethod, String path, String grpcService, String grpcMethod) {
    return RouteRegistryEntity.builder()
        .id(UUID.randomUUID())
        .httpMethod(httpMethod)
        .path(path)
        .grpcService(grpcService)
        .grpcMethod(grpcMethod)
        .requestType("com.gdn.project.waroenk.TestRequest")
        .responseType("com.gdn.project.waroenk.TestResponse")
        .publicEndpoint(false)
        .routeHash(UUID.randomUUID().toString().replace("-", "").substring(0, 64))
        .build();
  }

  /**
   * Creates a public RouteRegistryEntity.
   */
  public static RouteRegistryEntity createPublicRoute(String httpMethod, String path) {
    RouteRegistryEntity route = createRoute(httpMethod, path, "TestService", "TestMethod");
    route.setPublicEndpoint(true);
    return route;
  }

  /**
   * Creates a RouteRegistryEntity with required roles.
   */
  public static RouteRegistryEntity createProtectedRoute(String httpMethod, String path, List<String> roles) {
    RouteRegistryEntity route = createRoute(httpMethod, path, "TestService", "TestMethod");
    route.setPublicEndpoint(false);
    route.setRequiredRolesList(roles);
    return route;
  }

  /**
   * Creates a RouteRegistryEntity and attaches it to a service.
   */
  public static RouteRegistryEntity createRouteForService(ServiceRegistryEntity service, String httpMethod, String path) {
    RouteRegistryEntity route = createRoute(httpMethod, path, "TestService", "TestMethod");
    route.setService(service);
    service.getRoutes().add(route);
    return route;
  }

  // ============================================================
  // Dynamic Routing Registry DTOs
  // ============================================================

  /**
   * Creates a ServiceDefinition for registration.
   */
  public static DynamicRoutingRegistry.ServiceDefinition createServiceDefinition(String name) {
    return new DynamicRoutingRegistry.ServiceDefinition(
        name,
        "grpc",
        "localhost",
        9090,
        false,
        null,
        "1.0.0",
        List.of(createRouteDefinitionDto())
    );
  }

  /**
   * Creates a ServiceDefinition with multiple routes.
   */
  public static DynamicRoutingRegistry.ServiceDefinition createServiceDefinition(
      String name, String host, int port, List<DynamicRoutingRegistry.RouteDefinitionDto> routes) {
    return new DynamicRoutingRegistry.ServiceDefinition(
        name,
        "grpc",
        host,
        port,
        false,
        null,
        "1.0.0",
        routes
    );
  }

  /**
   * Creates a RouteDefinitionDto.
   */
  public static DynamicRoutingRegistry.RouteDefinitionDto createRouteDefinitionDto() {
    return new DynamicRoutingRegistry.RouteDefinitionDto(
        "GET",
        "/api/test/" + faker.lorem().word(),
        "com.gdn.project.waroenk.TestService",
        "TestMethod",
        "TestRequest",
        "TestResponse",
        false,
        List.of()
    );
  }

  /**
   * Creates a RouteDefinitionDto with specific details.
   */
  public static DynamicRoutingRegistry.RouteDefinitionDto createRouteDefinitionDto(
      String httpMethod, String path, String grpcService, String grpcMethod, boolean isPublic) {
    return new DynamicRoutingRegistry.RouteDefinitionDto(
        httpMethod,
        path,
        grpcService,
        grpcMethod,
        grpcService + "Request",
        grpcService + "Response",
        isPublic,
        List.of()
    );
  }

  /**
   * Creates a CachedService.
   */
  public static DynamicRoutingRegistry.CachedService createCachedService(String name) {
    return new DynamicRoutingRegistry.CachedService(
        name,
        "localhost",
        9090,
        false,
        LocalDateTime.now()
    );
  }

  /**
   * Creates a CachedRoute.
   */
  public static DynamicRoutingRegistry.CachedRoute createCachedRoute(
      String serviceName, String httpMethod, String path) {
    DynamicRoutingRegistry.CachedService service = createCachedService(serviceName);
    return new DynamicRoutingRegistry.CachedRoute(
        serviceName,
        "TestService",
        "TestMethod",
        httpMethod,
        path,
        "TestRequest",
        "TestResponse",
        false,
        List.of(),
        service
    );
  }

  // ============================================================
  // Utility Methods
  // ============================================================

  /**
   * Generates a random service name.
   */
  public static String randomServiceName() {
    return "service-" + faker.number().digits(4);
  }

  /**
   * Generates a random API path.
   */
  public static String randomApiPath() {
    return "/api/" + faker.lorem().word() + "/" + faker.lorem().word();
  }

  /**
   * Generates a random UUID string.
   */
  public static String randomUuidString() {
    return UUID.randomUUID().toString();
  }
}

