package ru.otus.kafka.obukhov.term.delivery.dto;
import lombok.*;


import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderDetailsDto {
    private UUID id;
    private UUID customerId;
    private UUID restaurantId;
    private String status;
    private BigDecimal totalAmount;
    private OffsetDateTime createdAt;
}