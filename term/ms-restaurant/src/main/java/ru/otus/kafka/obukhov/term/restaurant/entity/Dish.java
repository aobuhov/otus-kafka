package ru.otus.kafka.obukhov.term.restaurant.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dish")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;

    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RestaurantDish> restaurants = new ArrayList<>();
}