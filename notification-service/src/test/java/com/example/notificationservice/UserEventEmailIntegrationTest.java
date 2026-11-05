package com.example.notificationservice;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class UserEventEmailIntegrationTest {

    private static final String TOPIC = "user-events";

    @Container
    static KafkaContainer kafka = new KafkaContainer("apache/kafka:3.8.0");

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication());

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> ServerSetupTest.SMTP.getPort());
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void clearMailbox() throws Exception {
        greenMail.purgeEmailFromAllMailboxes();
    }

    @Test
    void sendsEmailWhenUserCreated() throws Exception {
        String email = "created@example.com";

        kafkaTemplate.send(TOPIC, email, json("CREATED", email));

        MimeMessage message = waitForMessage();
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo(email);
        assertThat(((String) message.getContent()).trim())
                .isEqualTo("Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.");
    }

    @Test
    void sendsEmailWhenUserDeleted() throws Exception {
        String email = "deleted@example.com";

        kafkaTemplate.send(TOPIC, email, json("DELETED", email));

        MimeMessage message = waitForMessage();
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo(email);
        assertThat(((String) message.getContent()).trim())
                .isEqualTo("Здравствуйте! Ваш аккаунт был удалён.");
    }

    private MimeMessage waitForMessage() {
        assertThat(greenMail.waitForIncomingEmail(30000, 1)).isTrue();
        return greenMail.getReceivedMessages()[0];
    }

    private String json(String operation, String email) {
        return "{\"operation\":\"" + operation + "\",\"email\":\"" + email + "\"}";
    }
}