package ru.otus.kafka.obukhov.term.delivery.event;

import lombok.*;

import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryEvent {
    private UUID deliveryId;
    private UUID orderId;
    private UUID employeeId;
    private String status;  // ASSIGNED / PICKED_UP / COMPLETED
}