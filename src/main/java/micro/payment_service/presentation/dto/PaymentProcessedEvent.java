package micro.payment_service.presentation.dto;

import micro.payment_service.domain.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentProcessedEvent(UUID paymentId,
                                    UUID orderId,
                                    String customerId,
                                    BigDecimal amount,
                                    String status) {
}
