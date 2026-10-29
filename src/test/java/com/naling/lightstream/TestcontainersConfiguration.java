package com.naling.lightstream;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>("redis:7").withExposedPorts(6379);
    }

    @Bean
    GenericContainer<?> storageContainer() {
        return new GenericContainer<>("rustfs/rustfs:1.0.4")
                .withExposedPorts(9000)
                .withEnv("RUSTFS_ACCESS_KEY", "lightstream")
                .withEnv("RUSTFS_SECRET_KEY", "lightstream-secret")
                .waitingFor(Wait.forListeningPort());
    }

    @Bean
    DynamicPropertyRegistrar storageProperties(GenericContainer<?> storageContainer) {
        return registry -> registry.add("lightstream.storage.endpoint",
                () -> "http://" + storageContainer.getHost() + ":" + storageContainer.getMappedPort(9000));
    }
}
