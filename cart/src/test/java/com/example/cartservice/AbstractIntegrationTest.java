package com.example.cartservice;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
public abstract class AbstractIntegrationTest {

    static {
        // Disable Ryuk to avoid issues with Rancher Desktop / privileges
        System.setProperty("TESTCONTAINERS_RYUK_DISABLED", "true");
    }

    @SuppressWarnings("resource")
    @Container
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:8.4.0"))
            .withExposedPorts(6379)
            .waitingFor(Wait.forListeningPort()) // ensure Redis is ready
            .withReuse(true); // speeds up repeated tests
}
