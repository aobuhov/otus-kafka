package ru.otus.kafka.obukhov.term.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "restaurant_dish")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RestaurantDish {

    @EmbeddedId
    private RestaurantDishId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("restaurantId")   // маппит id.restaurantId → restaurant.id
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("dishId")          // маппит id.dishId → dish.id
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    @Column(nullable = false)
    private Integer cnt;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}