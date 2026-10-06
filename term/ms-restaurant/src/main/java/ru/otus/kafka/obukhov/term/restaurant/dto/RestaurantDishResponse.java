package ru.otus.kafka.obukhov.term.restaurant.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RestaurantDishResponse {
    private UUID restaurantId;
    private String restaurantName;
    private UUID dishId;
    private String dishName;
    private Integer cnt;
    private BigDecimal price;
}
