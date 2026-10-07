package ru.otus.kafka.obukhov.term.restaurant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class RestaurantDishId implements Serializable {

    @Column(name = "restaurant_id")
    private UUID restaurantId;

    @Column(name = "dish_id")
    private UUID dishId;
}