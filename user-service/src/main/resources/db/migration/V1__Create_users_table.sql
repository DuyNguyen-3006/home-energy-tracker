CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100),
                       email VARCHAR(255) NOT NULL UNIQUE,
                       address TEXT,
                       alerting BOOLEAN NOT NULL DEFAULT FALSE,
                       energy_alerting_threshold DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                           ON UPDATE CURRENT_TIMESTAMP
);