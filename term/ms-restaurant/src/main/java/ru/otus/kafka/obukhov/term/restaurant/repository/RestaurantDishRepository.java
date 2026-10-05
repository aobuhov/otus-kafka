package ru.otus.kafka.obukhov.term.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.otus.kafka.obukhov.term.restaurant.entity.RestaurantDish;
import ru.otus.kafka.obukhov.term.restaurant.entity.RestaurantDishId;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RestaurantDishRepository extends JpaRepository<RestaurantDish, RestaurantDishId> {

    List<RestaurantDish> findByRestaurantId(UUID restaurantId);

    // Для проверки остатков по заказу
    @Query("""
        SELECT rd FROM RestaurantDish rd
        WHERE rd.restaurant.id = :restaurantId
          AND rd.dish.id IN :dishIds
        """)
    List<RestaurantDish> findByRestaurantAndDishIds(
            @Param("restaurantId") UUID restaurantId,
            @Param("dishIds") Collection<UUID> dishIds);
}