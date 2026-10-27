package micro.payment_service.application.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import micro.payment_service.domain.Payment;
import micro.payment_service.domain.PaymentStatus;
import micro.payment_service.domain.ProcessedEvent;
import micro.payment_service.infra.persistence.PaymentRepository;
import micro.payment_service.infra.persistence.ProcessedEventRepository;
import micro.payment_service.presentation.dto.OrderCreatedEvent;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ProcessedEventRepository processedEventRepository;

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

        log.info("Pagamento processado com sucesso para o pedido: {}", event.orderId());
    }
}