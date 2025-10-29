CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(64) PRIMARY KEY,
    phone VARCHAR(32) NOT NULL UNIQUE,
    plan_id VARCHAR(64) NULL,
    remaining_quota INT NOT NULL DEFAULT 0,
    auth_secret VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_phone (phone)
);

CREATE TABLE IF NOT EXISTS copy_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(64) NULL,
    client_key VARCHAR(128) NULL,
    image_id VARCHAR(255) NOT NULL,
    instruction TEXT NULL,
    copy_text TEXT NOT NULL,
    source VARCHAR(32) NOT NULL,
    sequence_no INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_copy_history_user_image (user_id, image_id),
    INDEX idx_copy_history_client_image (client_key, image_id),
    INDEX idx_copy_history_user_created (user_id, created_at),
    INDEX idx_copy_history_client_created (client_key, created_at)
);
