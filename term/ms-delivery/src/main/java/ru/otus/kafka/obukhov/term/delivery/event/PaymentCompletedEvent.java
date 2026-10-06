package ru.otus.kafka.obukhov.term.delivery.event;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCompletedEvent {
    private UUID orderId;
    private UUID paymentId;
    private String status;
}