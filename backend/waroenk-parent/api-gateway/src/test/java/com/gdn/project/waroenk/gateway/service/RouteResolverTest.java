package com.gdn.project.waroenk.gateway.service;

import com.gdn.project.waroenk.gateway.config.GatewayProperties;
import com.gdn.project.waroenk.gateway.fixture.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RouteResolver Unit Tests")
class RouteResolverTest {

  @Mock
  private GatewayProperties gatewayProperties;

  @Mock
  private DynamicRoutingRegistry dynamicRegistry;

  private RouteResolver routeResolver;

  @BeforeEach
  void setUp() {
    // Set up default mock behavior
    when(gatewayProperties.getRoutes()).thenReturn(List.of());
    when(gatewayProperties.getServices()).thenReturn(Map.of());

    routeResolver = new RouteResolver(gatewayProperties, dynamicRegistry);
    routeResolver.buildStaticRouteCache();
  }

  @Nested
  @DisplayName("resolve Tests")
  class ResolveTests {

    @Test
    @DisplayName("Should resolve route from dynamic registry first")
    void shouldResolveDynamicRouteFirst() {
      // Given
      DynamicRoutingRegistry.CachedRoute cachedRoute = TestDataFactory.createCachedRoute(
          "member-service", "GET", "/api/users");

      when(dynamicRegistry.resolveRoute("GET", "/api/users"))
          .thenReturn(Optional.of(cachedRoute));

      // When
      Optional<RouteResolver.ResolvedRoute> result = routeResolver.resolve("GET", "/api/users");

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().serviceName()).isEqualTo("member-service");
    }

    @Test
    @DisplayName("Should resolve route from static config when dynamic not found")
    void shouldResolveStaticRouteWhenDynamicNotFound() {
      // Given
      GatewayProperties.MethodMapping methodMapping = new GatewayProperties.MethodMapping();
      methodMapping.setHttpMethod("POST");
      methodMapping.setHttpPath("/api/auth/login");
      methodMapping.setGrpcMethod("Login");
      methodMapping.setRequestType("LoginRequest");
      methodMapping.setResponseType("LoginResponse");
      methodMapping.setPublicEndpoint(true);

      GatewayProperties.RouteConfig routeConfig = new GatewayProperties.RouteConfig();
      routeConfig.setService("auth-service");
      routeConfig.setGrpcService("com.gdn.auth.AuthService");
      routeConfig.setMethods(List.of(methodMapping));

      when(gatewayProperties.getRoutes()).thenReturn(List.of(routeConfig));
      when(dynamicRegistry.resolveRoute(anyString(), anyString())).thenReturn(Optional.empty());

      // Rebuild cache with new routes
      routeResolver.buildStaticRouteCache();

      // When
      Optional<RouteResolver.ResolvedRoute> result = routeResolver.resolve("POST", "/api/auth/login");

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().serviceName()).isEqualTo("auth-service");
      assertThat(result.get().grpcMethodName()).isEqualTo("Login");
      assertThat(result.get().publicEndpoint()).isTrue();
    }

    @Test
    @DisplayName("Should return empty when route not found anywhere")
    void shouldReturnEmptyWhenRouteNotFound() {
      // Given
      when(dynamicRegistry.resolveRoute(anyString(), anyString())).thenReturn(Optional.empty());

      // When
      Optional<RouteResolver.ResolvedRoute> result = routeResolver.resolve("GET", "/api/nonexistent");

      // Then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should resolve route with path pattern matching")
    void shouldResolveRouteWithPathPattern() {
      // Given
      GatewayProperties.MethodMapping methodMapping = new GatewayProperties.MethodMapping();
      methodMapping.setHttpMethod("GET");
      methodMapping.setHttpPath("/api/users/{id}");
      methodMapping.setGrpcMethod("GetUserById");

      GatewayProperties.RouteConfig routeConfig = new GatewayProperties.RouteConfig();
      routeConfig.setService("member-service");
      routeConfig.setGrpcService("com.gdn.member.UserService");
      routeConfig.setMethods(List.of(methodMapping));

      when(gatewayProperties.getRoutes()).thenReturn(List.of(routeConfig));
      when(dynamicRegistry.resolveRoute(anyString(), anyString())).thenReturn(Optional.empty());

      // Rebuild cache
      routeResolver.buildStaticRouteCache();

      // When
      Optional<RouteResolver.ResolvedRoute> result = routeResolver.resolve("GET", "/api/users/123");

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().grpcMethodName()).isEqualTo("GetUserById");
    }
  }

  @Nested
  @DisplayName("getServiceInfo Tests")
  class GetServiceInfoTests {

    @Test
    @DisplayName("Should return service info from dynamic registry first")
    void shouldReturnDynamicServiceInfo() {
      // Given
      DynamicRoutingRegistry.CachedService cachedService = TestDataFactory.createCachedService("member-service");
      when(dynamicRegistry.getService("member-service")).thenReturn(Optional.of(cachedService));

      // When
      Optional<RouteResolver.ServiceInfo> result = routeResolver.getServiceInfo("member-service");

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().name()).isEqualTo("member-service");
      assertThat(result.get().host()).isEqualTo("localhost");
    }

    @Test
    @DisplayName("Should return service info from static config when dynamic not found")
    void shouldReturnStaticServiceInfo() {
      // Given
      GatewayProperties.ServiceConfig serviceConfig = new GatewayProperties.ServiceConfig();
      serviceConfig.setHost("static-host");
      serviceConfig.setPort(9091);
      serviceConfig.setUseTls(true);

      when(dynamicRegistry.getService("catalog-service")).thenReturn(Optional.empty());
      when(gatewayProperties.getServices()).thenReturn(Map.of("catalog-service", serviceConfig));

      // When
      Optional<RouteResolver.ServiceInfo> result = routeResolver.getServiceInfo("catalog-service");

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().host()).isEqualTo("static-host");
      assertThat(result.get().port()).isEqualTo(9091);
      assertThat(result.get().useTls()).isTrue();
    }

    @Test
    @DisplayName("Should return empty when service not found anywhere")
    void shouldReturnEmptyWhenServiceNotFound() {
      // Given
      when(dynamicRegistry.getService("unknown-service")).thenReturn(Optional.empty());
      when(gatewayProperties.getServices()).thenReturn(Map.of());

      // When
      Optional<RouteResolver.ServiceInfo> result = routeResolver.getServiceInfo("unknown-service");

      // Then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("getAllRoutes Tests")
  class GetAllRoutesTests {

    @Test
    @DisplayName("Should return combined routes from static and dynamic sources")
    void shouldReturnCombinedRoutes() {
      // Given
      GatewayProperties.MethodMapping methodMapping = new GatewayProperties.MethodMapping();
      methodMapping.setHttpMethod("GET");
      methodMapping.setHttpPath("/api/static");
      methodMapping.setGrpcMethod("StaticMethod");

      GatewayProperties.RouteConfig routeConfig = new GatewayProperties.RouteConfig();
      routeConfig.setService("static-service");
      routeConfig.setGrpcService("StaticService");
      routeConfig.setMethods(List.of(methodMapping));

      when(gatewayProperties.getRoutes()).thenReturn(List.of(routeConfig));

      DynamicRoutingRegistry.CachedRoute dynamicRoute = TestDataFactory.createCachedRoute(
          "dynamic-service", "POST", "/api/dynamic");
      when(dynamicRegistry.getAllRoutes()).thenReturn(List.of(dynamicRoute));

      // Rebuild cache
      routeResolver.buildStaticRouteCache();

      // When
      List<RouteResolver.ResolvedRoute> allRoutes = routeResolver.getAllRoutes();

      // Then
      assertThat(allRoutes).hasSize(2);
    }
  }

  @Nested
  @DisplayName("extractPathVariables Tests")
  class ExtractPathVariablesTests {

    @Test
    @DisplayName("Should extract single path variable")
    void shouldExtractSinglePathVariable() {
      // When
      Map<String, String> variables = routeResolver.extractPathVariables("/api/users/{id}", "/api/users/123");

      // Then
      assertThat(variables).containsEntry("id", "123");
    }

    @Test
    @DisplayName("Should extract multiple path variables")
    void shouldExtractMultiplePathVariables() {
      // When
      Map<String, String> variables = routeResolver.extractPathVariables(
          "/api/users/{userId}/orders/{orderId}",
          "/api/users/123/orders/456"
      );

      // Then
      assertThat(variables)
          .containsEntry("userId", "123")
          .containsEntry("orderId", "456");
    }

    @Test
    @DisplayName("Should return empty map when no variables")
    void shouldReturnEmptyMapWhenNoVariables() {
      // When
      Map<String, String> variables = routeResolver.extractPathVariables("/api/users", "/api/users");

      // Then
      assertThat(variables).isEmpty();
    }
  }
}

