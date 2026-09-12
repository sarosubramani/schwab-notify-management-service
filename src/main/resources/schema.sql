CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(255) PRIMARY KEY,
    source_system VARCHAR(255) NOT NULL,
    correlation_id VARCHAR(255) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    recipients TEXT NOT NULL,
    channels TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    scheduled_at TIMESTAMP,
    expires_at TIMESTAMP,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    delivery_attempts TEXT,
    received_at TIMESTAMP
);
