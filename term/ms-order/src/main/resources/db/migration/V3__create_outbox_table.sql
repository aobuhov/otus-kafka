CREATE TABLE outbox_event (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              aggregate_id UUID NOT NULL,
                              aggregate_type VARCHAR(50) NOT NULL,
                              event_type VARCHAR(100) NOT NULL,
                              topic VARCHAR(100) NOT NULL,
                              payload JSONB NOT NULL,
                              status VARCHAR(20) NOT NULL,
                              attempts INT NOT NULL DEFAULT 0,
                              last_error VARCHAR(1000),
                              created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              published_at TIMESTAMP WITH TIME ZONE,
                              version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_outbox_status_created_at ON outbox_event(status, created_at);