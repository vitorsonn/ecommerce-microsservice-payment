package micro.payment_service.infra.messaging;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import micro.payment_service.application.service.PaymentService;
import micro.payment_service.infra.config.RabbitMQConfig;
import micro.payment_service.presentation.dto.OrderCreatedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentConsumer {
    private final PaymentService paymentService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAYMENT_PROCESS)
    public void consumeOrderCreatedEvent(OrderCreatedEvent event){
        log.info("Mensagem recebida do RabbitMQ: {}", event);
        paymentService.processPayment(event);
    }
}
