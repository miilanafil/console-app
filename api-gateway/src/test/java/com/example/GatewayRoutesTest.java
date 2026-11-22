package com.example;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest(
        classes = {ApiGatewayApplication.class, GatewayRoutesTest.LoadBalancerTestConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.config.enabled=false",
                "spring.cloud.gateway.httpclient.pool.type=disabled",
                "logging.level.org.springframework.cloud.gateway=TRACE",
                "logging.level.org.springframework.web.server.adapter.HttpWebHandlerAdapter=TRACE"
        }
)
class GatewayRoutesTest {

    static WireMockServer wireMockServer = new WireMockServer(
            wireMockConfig().dynamicPort().bindAddress("127.0.0.1")
    );

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeAll
    static void startWireMock() {
        wireMockServer.start();
        configureFor("127.0.0.1", wireMockServer.port());
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();
        // HttpClient.newConnection() полностью отключает пул сокетов у тестового клиента
        this.webTestClient = WebTestClient
                .bindToServer(new ReactorClientHttpConnector(HttpClient.newConnection()))
                .baseUrl("http://127.0.0.1:" + port)
                .responseTimeout(Duration.ofSeconds(10))
                .build();
    }

    @TestConfiguration
    @LoadBalancerClients(defaultConfiguration = GatewayRoutesTest.DefaultLbConfig.class)
    static class LoadBalancerTestConfig {
    }

    static class DefaultLbConfig {
        @Bean
        public ServiceInstanceListSupplier serviceInstanceListSupplier() {
            return new ServiceInstanceListSupplier() {
                @Override
                public String getServiceId() {
                    return "default-mock-service";
                }

                @Override
                public Flux<List<ServiceInstance>> get() {
                    return Flux.just(List.of(
                            new DefaultServiceInstance("instance-1", "mock", "127.0.0.1", wireMockServer.port(), false)
                    ));
                }
            };
        }
    }

    @Test
    void shouldRouteToUserService() {
        stubFor(get(urlEqualTo("/users/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": 1, \"name\": \"Ivan\"}")));

        webTestClient.get()
                .uri("/api/users/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("Ivan");
    }

    @Test
    void shouldRouteToNotificationService() {
        stubFor(get(urlEqualTo("/notifications/10"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": 10, \"status\": \"SENT\"}")));

        webTestClient.get()
                .uri("/api/notifications/10")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(10)
                .jsonPath("$.status").isEqualTo("SENT");
    }

    @Test
    void shouldTriggerUserFallbackOnTimeout() {
        stubFor(get(urlEqualTo("/users/99"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(4000)));

        webTestClient.get()
                .uri("/api/users/99")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Пользовательский сервис верменно недоступен");
    }

    @Test
    void shouldTriggerNotificationFallbackOnTimeout() {
        stubFor(get(urlEqualTo("/notifications/99"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(4000)));

        webTestClient.get()
                .uri("/api/notifications/99")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Сервис уведомлений верменно недоступен");
    }
}