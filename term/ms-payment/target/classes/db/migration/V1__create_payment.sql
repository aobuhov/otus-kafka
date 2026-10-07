CREATE TABLE payment (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         order_id UUID NOT NULL UNIQUE,
                         customer_id UUID NOT NULL,
                         amount DECIMAL(10, 2) NOT NULL,
                         status VARCHAR(50) NOT NULL,
                         created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_order_id ON payment(order_id);
CREATE INDEX idx_payment_status ON payment(status);