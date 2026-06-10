CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    phone VARCHAR(32),
    status VARCHAR(16) NOT NULL DEFAULT 'enabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE venues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(32),
    open_time TIME NOT NULL,
    close_time TIME NOT NULL,
    owner_user_id BIGINT,
    status VARCHAR(16) NOT NULL DEFAULT 'enabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_venue_owner FOREIGN KEY (owner_user_id) REFERENCES users(id)
);

CREATE UNIQUE INDEX uk_owner_user_id ON venues(owner_user_id);
CREATE INDEX idx_venue_owner ON venues(owner_user_id);

CREATE TABLE courts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    venue_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    type VARCHAR(32) NOT NULL,
    price_per_slot DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(16) NOT NULL DEFAULT 'enabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_court_venue FOREIGN KEY (venue_id) REFERENCES venues(id)
);

CREATE INDEX idx_court_venue ON courts(venue_id);

CREATE TABLE slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    court_id BIGINT NOT NULL,
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'available',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_slot_court FOREIGN KEY (court_id) REFERENCES courts(id)
);

CREATE INDEX idx_slot_court_date ON slots(court_id, slot_date);

CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    venue_id BIGINT NOT NULL,
    court_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    canceled_at TIMESTAMP NULL,
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_order_venue FOREIGN KEY (venue_id) REFERENCES venues(id),
    CONSTRAINT fk_order_court FOREIGN KEY (court_id) REFERENCES courts(id),
    CONSTRAINT fk_order_slot FOREIGN KEY (slot_id) REFERENCES slots(id)
);

CREATE INDEX idx_order_user ON orders(user_id);
CREATE INDEX idx_order_venue ON orders(venue_id);
CREATE INDEX idx_order_slot ON orders(slot_id);

CREATE TABLE slot_reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    slot_id BIGINT NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'locked',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sr_slot FOREIGN KEY (slot_id) REFERENCES slots(id),
    CONSTRAINT fk_sr_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE INDEX idx_sr_order ON slot_reservations(order_id);
