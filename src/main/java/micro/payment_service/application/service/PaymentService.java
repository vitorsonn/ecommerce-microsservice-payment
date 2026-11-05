package micro.payment_service.application.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import micro.payment_service.domain.Payment;
import micro.payment_service.domain.PaymentStatus;
import micro.payment_service.domain.ProcessedEvent;
import micro.payment_service.infra.config.RabbitMQConfig;
import micro.payment_service.infra.persistence.PaymentRepository;
import micro.payment_service.infra.persistence.ProcessedEventRepository;
import micro.payment_service.presentation.dto.OrderCreatedEvent;
import micro.payment_service.presentation.dto.PaymentProcessedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public void processPayment(OrderCreatedEvent event) {
        if (processedEventRepository.existsById(event.orderId())) {
            log.warn("Evento com o ID {} já foi processado: {}", event.orderId(), event);
            return;
        }

        log.info("Processando pagamento para o pedido: {}", event.orderId());

        Payment payment = Payment.builder()
                .orderId(event.orderId())
                .customerId(event.customerId())
                .amount(event.totalAmount())
                .status(PaymentStatus.APROVADO)
                .build();

        paymentRepository.save(payment);

        ProcessedEvent processedEvent = ProcessedEvent.builder()
                .eventId(event.orderId())
                .build();

        processedEventRepository.save(processedEvent);

        PaymentProcessedEvent processedEventDTO = new PaymentProcessedEvent(payment.getId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getStatus().name());

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_PAYMENTS,
                RabbitMQConfig.ROUTING_KEY_PAYMENT_PROCESSED,
                processedEventDTO);

        log.info("Pagamento processado com sucesso para o pedido: {}", event.orderId());
    }

}