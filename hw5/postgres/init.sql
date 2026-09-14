CREATE TABLE events (
    id uuid PRIMARY KEY ,
    event_type INT NOT NULL,
    event_description VARCHAR(64) NULL,
    created_at TIMESTAMP NOT NULL,
    modify_at TIMESTAMP NOT NULL
)