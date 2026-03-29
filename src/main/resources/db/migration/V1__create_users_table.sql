CREATE TABLE users (
    id            BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100)    NOT NULL,
    email         VARCHAR(255)    NOT NULL UNIQUE,
    password      VARCHAR(255)    NOT NULL,
    role          VARCHAR(10)     NOT NULL DEFAULT 'USER',
    is_active     BOOLEAN         NOT NULL DEFAULT TRUE,
    refresh_token VARCHAR(500),
    created_at    TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP       NOT NULL DEFAULT NOW() ON UPDATE NOW()
);