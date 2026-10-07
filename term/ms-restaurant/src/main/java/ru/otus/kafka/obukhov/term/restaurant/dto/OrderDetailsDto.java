package ru.otus.kafka.obukhov.term.restaurant.dto;
import lombok.*;


import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderDetailsDto {
    private UUID id;
    private UUID restaurantId;
    private List<OrderItemDto> items;
}