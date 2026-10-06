package ru.otus.kafka.obukhov.term.payment.event;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResultEvent {
    private UUID orderId;
    private UUID paymentId;
    private String status;  // PAID / ABORTED / CANCELLED
    private String reason;
}