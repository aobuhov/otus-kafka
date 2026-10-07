package ru.otus.kafka.obukhov.term.restaurant.event;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantOrderDecisionEvent {
    private UUID orderId;
    private UUID restaurantId;
    private String reason;  // для rejected
    private UUID eventId;
}