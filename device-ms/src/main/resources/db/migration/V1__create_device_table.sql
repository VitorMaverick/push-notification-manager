CREATE TABLE IF NOT EXISTS device (
    id           BIGSERIAL PRIMARY KEY,
    fcm_token    VARCHAR(512)  NOT NULL UNIQUE,
    device_name  VARCHAR(255),
    type         VARCHAR(50),
    status       VARCHAR(50)   NOT NULL,
    registered_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_used_at  TIMESTAMP WITH TIME ZONE
);
