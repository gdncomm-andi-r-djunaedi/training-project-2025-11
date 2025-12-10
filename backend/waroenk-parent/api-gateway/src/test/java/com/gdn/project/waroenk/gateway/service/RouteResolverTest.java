package com.gdn.project.waroenk.gateway.service;

import com.gdn.project.waroenk.gateway.config.GatewayProperties;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RouteResolver Unit Tests")
class RouteResolverTest {

    @Mock
    private GatewayProperties gatewayProperties;

    private StaticRouteRegistry routeRegistry;
    private RouteResolver routeResolver;

    @BeforeEach
    void setUp() {
        // Set up default mock behavior
        when(gatewayProperties.getRoutes()).thenReturn(List.of());
        when(gatewayProperties.getServices()).thenReturn(Map.of());

        routeRegistry = new StaticRouteRegistry(gatewayProperties);
        routeRegistry.initialize();
        routeResolver = new RouteResolver(routeRegistry);
    }

    @Nested
    @DisplayName("resolve Tests")
    class ResolveTests {

        @Test
        @DisplayName("Should resolve route from static config")
        void shouldResolveStaticRoute() {
            // Given
            GatewayProperties.MethodMapping methodMapping = new GatewayProperties.MethodMapping();
            methodMapping.setHttpMethod("POST");
            methodMapping.setHttpPath("/api/auth/login");
            methodMapping.setGrpcMethod("Login");
            methodMapping.setPublicEndpoint(true);

            GatewayProperties.RouteConfig routeConfig = new GatewayProperties.RouteConfig();
            routeConfig.setService("auth-service");
            routeConfig.setGrpcService("com.gdn.auth.AuthService");
            routeConfig.setMethods(List.of(methodMapping));

            when(gatewayProperties.getRoutes()).thenReturn(List.of(routeConfig));

            // Rebuild registry with new routes
            routeRegistry = new StaticRouteRegistry(gatewayProperties);
            routeRegistry.initialize();
            routeResolver = new RouteResolver(routeRegistry);

            // When
            Optional<RouteResolver.ResolvedRoute> result = routeResolver.resolve("POST", "/api/auth/login");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().serviceName()).isEqualTo("auth-service");
            assertThat(result.get().grpcMethodName()).isEqualTo("Login");
            assertThat(result.get().publicEndpoint()).isTrue();
        }

        @Test
        @DisplayName("Should return empty when route not found")
        void shouldReturnEmptyWhenRouteNotFound() {
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

            // Rebuild registry
            routeRegistry = new StaticRouteRegistry(gatewayProperties);
            routeRegistry.initialize();
            routeResolver = new RouteResolver(routeRegistry);

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
        @DisplayName("Should return service info from static config")
        void shouldReturnStaticServiceInfo() {
            // Given
            GatewayProperties.ServiceConfig serviceConfig = new GatewayProperties.ServiceConfig();
            serviceConfig.setHost("static-host");
            serviceConfig.setPort(9091);
            serviceConfig.setUseTls(true);

            when(gatewayProperties.getServices()).thenReturn(Map.of("catalog-service", serviceConfig));

            // Rebuild registry
            routeRegistry = new StaticRouteRegistry(gatewayProperties);
            routeRegistry.initialize();
            routeResolver = new RouteResolver(routeRegistry);

            // When
            Optional<StaticRouteRegistry.ServiceInfo> result = routeResolver.getServiceInfo("catalog-service");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().host()).isEqualTo("static-host");
            assertThat(result.get().port()).isEqualTo(9091);
            assertThat(result.get().useTls()).isTrue();
        }

        @Test
        @DisplayName("Should return empty when service not found")
        void shouldReturnEmptyWhenServiceNotFound() {
            // When
            Optional<StaticRouteRegistry.ServiceInfo> result = routeResolver.getServiceInfo("unknown-service");

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAllRoutes Tests")
    class GetAllRoutesTests {

        @Test
        @DisplayName("Should return all configured routes")
        void shouldReturnAllRoutes() {
            // Given
            GatewayProperties.MethodMapping methodMapping1 = new GatewayProperties.MethodMapping();
            methodMapping1.setHttpMethod("GET");
            methodMapping1.setHttpPath("/api/users");
            methodMapping1.setGrpcMethod("GetUsers");

            GatewayProperties.MethodMapping methodMapping2 = new GatewayProperties.MethodMapping();
            methodMapping2.setHttpMethod("POST");
            methodMapping2.setHttpPath("/api/users");
            methodMapping2.setGrpcMethod("CreateUser");

            GatewayProperties.RouteConfig routeConfig = new GatewayProperties.RouteConfig();
            routeConfig.setService("member-service");
            routeConfig.setGrpcService("MemberService");
            routeConfig.setMethods(List.of(methodMapping1, methodMapping2));

            when(gatewayProperties.getRoutes()).thenReturn(List.of(routeConfig));

            // Rebuild registry
            routeRegistry = new StaticRouteRegistry(gatewayProperties);
            routeRegistry.initialize();
            routeResolver = new RouteResolver(routeRegistry);

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
