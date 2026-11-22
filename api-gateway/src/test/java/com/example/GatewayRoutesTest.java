package com.example;

import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;


@SpringBootTest(  classes = ApiGatewayApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {

                //notifications-service
                "spring.cloud.gateway.routes[0].id=notifications-service",
                "spring.cloud.gateway.routes[0].uri=http://localhost:${wiremock.server.port}",
                "spring.cloud.gateway.routes[0].predicates[0]=Path=/api/notifications/**",
                "spring.cloud.gateway.routes[0].filters[0]=StripPrefix=1",
                "spring.cloud.gateway.routes[0].filters[1].name=CircuitBreaker",
                "spring.cloud.gateway.routes[0].filters[1].args.name=notificationsServiceCircuitBreaker",
                "spring.cloud.gateway.routes[0].filters[1].args.fallbackUri=forward:/fallback/notifications",

                //user-service
                "spring.cloud.gateway.routes[1].id=user-service",
                "spring.cloud.gateway.routes[1].uri=http://localhost:${wiremock.server.port}",
                "spring.cloud.gateway.routes[1].predicates[0]=Path=/api/users/**",
                "spring.cloud.gateway.routes[1].filters[0]=StripPrefix=1",
                "spring.cloud.gateway.routes[1].filters[1].name=CircuitBreaker",
                "spring.cloud.gateway.routes[1].filters[1].args.name=userServiceCircuitBreaker",
                "spring.cloud.gateway.routes[1].filters[1].args.fallbackUri=forward:/fallback/users",

                // Откл Eureka и Spring Cloud
                "eureka.client.enabled=false",
                "spring.cloud.config.enabled=false"
        }
        )
@AutoConfigureWireMock(port = 0)
@AutoConfigureWebTestClient(timeout = "30000")
class GatewayRoutesTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldRouteToUserService() {
        stubFor(get(urlEqualTo("/users/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": 1, \"name\": \"Milana\"}")));////////////////////

        webTestClient.get()
                .uri("/api/users/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("Milana");

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
                        .withFixedDelay(4000))); // Больше 3s таймаута

        webTestClient.get()
                .uri("/api/notifications/99")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Сервис уведомлений верменно недоступен");
    }
}