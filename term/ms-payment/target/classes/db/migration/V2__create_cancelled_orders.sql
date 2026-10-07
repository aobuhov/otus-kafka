CREATE TABLE cancelled_orders (
                                  order_id UUID PRIMARY KEY,
                                  cancelled_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);