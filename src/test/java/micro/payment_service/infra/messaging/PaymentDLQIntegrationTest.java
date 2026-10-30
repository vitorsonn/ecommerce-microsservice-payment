package micro.payment_service.infra.messaging;


import micro.payment_service.application.service.PaymentService;
import micro.payment_service.infra.config.RabbitMQConfig;
import micro.payment_service.presentation.dto.OrderCreatedEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;

import java.math.BigDecimal;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
class PaymentDLQIntegrationTest {


    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.12-alpine");


    @DynamicPropertySource
    static void configureRabbit(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");


        registry.add("spring.rabbitmq.listener.simple.retry.enabled", () -> "true");
        registry.add("spring.rabbitmq.listener.simple.retry.max-attempts", () -> "3");
        registry.add("spring.rabbitmq.listener.simple.retry.initial-interval", () -> "1000ms");
        registry.add("spring.rabbitmq.listener.simple.retry.multiplier", () -> "2.0");
        registry.add("spring.rabbitmq.listener.simple.default-requeue-rejected", () -> "false");

    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    @DisplayName("Deve enviar a mensagem para a DLQ após atingir o número máximo de tentativas")
    void shouldSendToDlqAfterMaxRetries(){
        doThrow(new RuntimeException("Simulação de erro no processamento"))
                .when(paymentService).processPayment(any());

        OrderCreatedEvent event = new OrderCreatedEvent(UUID.randomUUID(), "usr-999", new BigDecimal("500.00"));

        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_PAYMENT_PROCESS, event);

        await().atMost(15, SECONDS).untilAsserted(() -> {
            // Verifica se o consumidor tentou processar 3 vezes
            verify(paymentService, times(3)).processPayment(any());
        });

        await().atMost(10, SECONDS).untilAsserted(() -> {
            Message message = rabbitTemplate.receive(RabbitMQConfig.DLQ_PAYMENT_PROCESS);
            assertNotNull(message, "A mensagem deveria estar na DLQ!");
        });






    }


}
