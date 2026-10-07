CREATE TABLE delivery (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          order_id UUID NOT NULL UNIQUE,
                          customer_id UUID NOT NULL,
                          employee_id UUID REFERENCES employee(id),
                          status VARCHAR(50) NOT NULL,
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_delivery_order_id ON delivery(order_id);
CREATE INDEX idx_delivery_employee_id ON delivery(employee_id);
CREATE INDEX idx_delivery_status ON delivery(status);