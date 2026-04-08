CREATE DATABASE IF NOT EXISTS db1 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE db1;

CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    subscription_tier VARCHAR(32) NOT NULL,
    enabled BIT NOT NULL,
    daily_request_quota INT NOT NULL,
    preferred_model VARCHAR(64) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_user_account_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_model_quota (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    model_name VARCHAR(64) NOT NULL,
    daily_quota INT NOT NULL,
    CONSTRAINT fk_user_model_quota_user FOREIGN KEY (user_id) REFERENCES user_account (id) ON DELETE CASCADE,
    CONSTRAINT uk_user_model_quota_user_model UNIQUE (user_id, model_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS conversation_session (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    manual_title BIT NOT NULL,
    last_message_preview VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_conversation_session_user FOREIGN KEY (user_id) REFERENCES user_account (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS conversation_message (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(32) NOT NULL,
    content TEXT NOT NULL,
    model_name VARCHAR(64) NULL,
    error_message VARCHAR(512) NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_conversation_message_session FOREIGN KEY (conversation_id) REFERENCES conversation_session (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS conversation_memory (
    conversation_id BIGINT NOT NULL PRIMARY KEY,
    messages_json LONGTEXT NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_conversation_memory_session FOREIGN KEY (conversation_id) REFERENCES conversation_session (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS api_call_record (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    model_name VARCHAR(64) NOT NULL,
    memory_id BIGINT NULL,
    request_length INT NOT NULL,
    response_length INT NOT NULL,
    request_preview VARCHAR(255) NULL,
    response_preview VARCHAR(255) NULL,
    latency_ms BIGINT NOT NULL,
    success BIT NOT NULL,
    error_message VARCHAR(512) NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_api_call_record_user FOREIGN KEY (user_id) REFERENCES user_account (id) ON DELETE CASCADE,
    CONSTRAINT fk_api_call_record_session FOREIGN KEY (memory_id) REFERENCES conversation_session (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
