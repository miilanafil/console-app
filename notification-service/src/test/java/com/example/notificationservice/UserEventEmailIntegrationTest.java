package com.example.notificationservice;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
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

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class UserEventEmailIntegrationTest {

    private static final String TOPIC = "user-events";
    private static final String FAILED_TOPIC = "user-events.DLT";

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

    @Test
    void brokenMessageGoesToFailedTopic() {
        String payload = "это не json " + UUID.randomUUID();

        kafkaTemplate.send(TOPIC, "broken", payload);

        ConsumerRecord<String, String> record = readFromFailedTopic(payload);
        assertThat(record.value()).isEqualTo(payload);
        assertThat(greenMail.getReceivedMessages()).isEmpty();
    }

    @Test
    void messageWithoutEmailGoesToFailedTopic() {
        String payload = "{\"operation\":\"CREATED\",\"note\":\"" + UUID.randomUUID() + "\"}";

        kafkaTemplate.send(TOPIC, "no-email", payload);

        ConsumerRecord<String, String> record = readFromFailedTopic(payload);
        assertThat(record.value()).isEqualTo(payload);
        assertThat(greenMail.getReceivedMessages()).isEmpty();
    }

    @Test
    void validMessageIsProcessedAfterBrokenOne() throws Exception {
        String email = "after-broken@example.com";

        kafkaTemplate.send(TOPIC, "broken", "сломанное сообщение " + UUID.randomUUID());
        kafkaTemplate.send(TOPIC, email, json("CREATED", email));

        MimeMessage message = waitForMessage();
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo(email);
        assertThat(greenMail.getReceivedMessages()).hasSize(1);
    }

    private MimeMessage waitForMessage() {
        assertThat(greenMail.waitForIncomingEmail(30000, 1)).isTrue();
        return greenMail.getReceivedMessages()[0];
    }

    private ConsumerRecord<String, String> readFromFailedTopic(String expectedValue) {
        Map<String, Object> config = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(config)) {
            consumer.subscribe(List.of(FAILED_TOPIC));
            long deadline = System.currentTimeMillis() + 30000;
            while (System.currentTimeMillis() < deadline) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    if (expectedValue.equals(record.value())) {
                        return record;
                    }
                }
            }
        }
        throw new AssertionError("В топике " + FAILED_TOPIC + " не появилось сообщение: " + expectedValue);
    }

    private String json(String operation, String email) {
        return "{\"operation\":\"" + operation + "\",\"email\":\"" + email + "\"}";
    }
}