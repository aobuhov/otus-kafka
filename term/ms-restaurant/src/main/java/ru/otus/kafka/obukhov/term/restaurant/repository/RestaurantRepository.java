package ru.otus.kafka.obukhov.term.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.kafka.obukhov.term.restaurant.entity.Restaurant;

import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

}