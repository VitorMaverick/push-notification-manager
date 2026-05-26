CREATE TABLE IF NOT EXISTS push_notification (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    body            TEXT         NOT NULL,
    recipient_token VARCHAR(512) NOT NULL,
    status          VARCHAR(50)  NOT NULL,
    fcm_message_id  VARCHAR(255),
    sent_at         TIMESTAMP WITH TIME ZONE,
    delivered_at    TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL
);
