package com.gdn.training.member.Integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.gdn.training.member.infrastructure.grpc.proto.MemberServiceGrpc;
import com.gdn.training.member.infrastructure.grpc.proto.MemberServiceProto;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class GrpcEndToEndIT {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15.4")
            .withDatabaseName("member_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    @SuppressWarnings("resource")
    static GenericContainer<?> redisContainer = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);
    static ManagedChannel channel;
    static MemberServiceGrpc.MemberServiceBlockingStub blockingStub;

    @DynamicPropertySource
    static void dynamicPropertySource(DynamicPropertyRegistry registry) {
        registry.add("DB_Host", postgresContainer::getHost);
        registry.add("DB_Port", postgresContainer::getFirstMappedPort);
        registry.add("DB_Name", postgresContainer::getDatabaseName);
        registry.add("DB_User", postgresContainer::getUsername);
        registry.add("DB_Pass", postgresContainer::getPassword);
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", redisContainer::getFirstMappedPort);
        registry.add("member.dummy.enabled", () -> "true");
        registry.add("member.dummy.count", () -> "50");
        registry.add("grpc.server.port", () -> "9095");
    }

    @BeforeAll
    public static void setup() {
        channel = ManagedChannelBuilder.forAddress("localhost", 9095)
                .usePlaintext()
                .build();
        blockingStub = MemberServiceGrpc.newBlockingStub(channel);
    }

    @AfterAll
    public static void teardown() {
        if (channel != null) {
            channel.shutdownNow();
        }
    }

    @Test
    public void registerAndLoginFlow() {
        // Register
        com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberRequest registerRequest = com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberRequest
                .newBuilder()
                .setFullName("John Doe")
                .setEmail("test@example.com")
                .setRawPassword("password")
                .setPhoneNumber("123456789")
                .build();
        com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberResponse registerResponse = blockingStub
                .registerMember(registerRequest);
        assertNotNull(registerResponse);
        assertFalse(registerResponse.getMemberId().isEmpty());
        // Login
        com.gdn.training.member.infrastructure.grpc.proto.LoginMemberRequest loginRequest = com.gdn.training.member.infrastructure.grpc.proto.LoginMemberRequest
                .newBuilder()
                .setEmail("test@example.com")
                .setRawPassword("password")
                .build();
        com.gdn.training.member.infrastructure.grpc.proto.LoginMemberResponse loginResponse = blockingStub
                .loginMember(loginRequest);
        assertNotNull(loginResponse);
        assertTrue(loginResponse.getSuccess());
    }

}
