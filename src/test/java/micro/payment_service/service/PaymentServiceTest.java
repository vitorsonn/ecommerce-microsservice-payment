package micro.payment_service.service;

import micro.payment_service.application.service.PaymentService;
import micro.payment_service.domain.Payment;
import micro.payment_service.domain.ProcessedEvent;
import micro.payment_service.infra.persistence.PaymentRepository;
import micro.payment_service.infra.persistence.ProcessedEventRepository;
import micro.payment_service.presentation.dto.OrderCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @InjectMocks
    private PaymentService paymentService;


    @Test
    @DisplayName("Deve processar o pagamento com sucesso quando for novo")
    void shouldProcessPaymentSuccessfully(){

        UUID orderId = UUID.randomUUID();
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, "usr-123", new BigDecimal("150.00"));

        when(processedEventRepository.existsById(orderId)).thenReturn(false);

        paymentService.processPayment(event);

        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(processedEventRepository, times(1)).save(any(ProcessedEvent.class));
    }

    @Test
    @DisplayName("Não deve processar o pagamento se já tiver sido processado")
    void shouldNotProcessPaymentIfAlreadyProcessed() {
        UUID orderId = UUID.randomUUID();
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, "usr-123", new BigDecimal("150.00"));

        when(processedEventRepository.existsById(orderId)).thenReturn(true);

        paymentService.processPayment(event);

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(processedEventRepository, never()).save(any(ProcessedEvent.class));
    }

    @Test
    @DisplayName("Deve lançar uma exceção quando o repositório falhar")
    void shouldThrowExceptionWhenRepositoryFails(){
        UUID orderId = UUID.randomUUID();
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, "usr-123", new BigDecimal("150.00"));

        when(processedEventRepository.existsById(orderId)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class)))
                .thenThrow(new RuntimeException("Falha de conexão com o banco de dados"));

        assertThrows(RuntimeException.class, () -> paymentService.processPayment(event));

        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(processedEventRepository, never()).save(any(ProcessedEvent.class));

    }

}
