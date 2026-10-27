package micro.payment_service.presentation.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreatedEvent(UUID orderId,
                                String customerId,
                                BigDecimal totalAmount) {
}
