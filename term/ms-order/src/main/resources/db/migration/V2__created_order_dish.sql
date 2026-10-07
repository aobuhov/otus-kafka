CREATE TABLE order_dish (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            dish_id UUID NOT NULL,
                            order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                            cnt INT NOT NULL CHECK (cnt > 0),
                            price DECIMAL(10, 2) NOT NULL,
                            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_dish_order_id ON order_dish(order_id);