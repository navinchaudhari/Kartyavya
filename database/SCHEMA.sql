CREATE DATABASE IF NOT EXISTS access_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS report_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE access_db;
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(190) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    mobile_number VARCHAR(15) NOT NULL,
    address VARCHAR(255),
    role VARCHAR(30) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);
CREATE TABLE departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL UNIQUE,
    contact_email VARCHAR(190) NOT NULL,
    description VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);
CREATE TABLE officer_department_assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    officer_id BIGINT NOT NULL UNIQUE,
    department_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    assigned_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_officer_user FOREIGN KEY (officer_id) REFERENCES users(id),
    CONSTRAINT fk_officer_department FOREIGN KEY (department_id) REFERENCES departments(id)
);
CREATE TABLE routing_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category VARCHAR(40) NOT NULL UNIQUE,
    department_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_rule_department FOREIGN KEY (department_id) REFERENCES departments(id)
);
CREATE TABLE password_otps (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(190) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_otp_email_created (email, created_at)
);

USE report_db;
CREATE TABLE reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tracking_code VARCHAR(20) NOT NULL UNIQUE,
    citizen_id BIGINT NOT NULL,
    citizen_name VARCHAR(120) NOT NULL,
    citizen_email VARCHAR(190) NOT NULL,
    citizen_mobile VARCHAR(15) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(3000) NOT NULL,
    area_location VARCHAR(255) NOT NULL,
    image_path VARCHAR(500) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    ai_category VARCHAR(40) NOT NULL,
    ai_severity VARCHAR(20) NOT NULL,
    ai_confidence DOUBLE NOT NULL,
    ai_overridden BOOLEAN NOT NULL DEFAULT FALSE,
    suggested_department_code VARCHAR(80),
    department_id BIGINT,
    department_name VARCHAR(120),
    department_email VARCHAR(190),
    officer_id BIGINT,
    officer_name VARCHAR(120),
    officer_email VARCHAR(190),
    officer_mobile VARCHAR(15),
    status VARCHAR(45) NOT NULL,
    pending_reason VARCHAR(500),
    resolution_remark VARCHAR(2000),
    resolution_image_path VARCHAR(500),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    resolved_at TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    INDEX idx_report_citizen (citizen_id),
    INDEX idx_report_officer (officer_id),
    INDEX idx_report_status (status),
    INDEX idx_report_location (latitude, longitude)
);
CREATE TABLE report_status_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_id BIGINT NOT NULL,
    from_status VARCHAR(45),
    to_status VARCHAR(45) NOT NULL,
    remarks VARCHAR(1000) NOT NULL,
    changed_by BIGINT NOT NULL,
    changed_by_role VARCHAR(30) NOT NULL,
    changed_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_history_report FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE
);
CREATE TABLE outbox_events (
    id VARCHAR(36) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    routing_key VARCHAR(100) NOT NULL,
    payload LONGTEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL,
    published_at TIMESTAMP(6),
    last_error VARCHAR(1000),
    INDEX idx_outbox_status_created (status, created_at)
);
