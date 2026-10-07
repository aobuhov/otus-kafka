CREATE TABLE restaurant_dish (
                                 restaurant_id UUID NOT NULL REFERENCES restaurant(id) ON DELETE CASCADE,
                                 dish_id UUID NOT NULL REFERENCES dish(id) ON DELETE CASCADE,
                                 cnt INT NOT NULL CHECK (cnt >= 0),
                                 price DECIMAL(10, 2) NOT NULL,
                                 PRIMARY KEY (restaurant_id, dish_id)
);

CREATE INDEX idx_restaurant_dish_dish_id ON restaurant_dish(dish_id);