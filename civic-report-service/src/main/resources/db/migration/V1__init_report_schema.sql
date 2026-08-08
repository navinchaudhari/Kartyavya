-- Owner: M2. Frozen DDL copied from docs/contracts/database-schema.md (Appendix A.2).

CREATE TABLE reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tracking_code CHAR(36) NOT NULL UNIQUE,
    reporter_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    latitude DECIMAL(9,6) NOT NULL,
    longitude DECIMAL(9,6) NOT NULL,
    category VARCHAR(40) NULL,
    severity VARCHAR(20) NULL,
    confidence_score DECIMAL(5,4) NULL,
    urgency_score DECIMAL(5,4) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
    assigned_department_id BIGINT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    INDEX idx_reports_reporter_created (reporter_id, created_at),
    INDEX idx_reports_status_created (status, created_at),
    INDEX idx_reports_department_status (assigned_department_id, status),
    INDEX idx_reports_category_status (category, status),
    INDEX idx_reports_location (latitude, longitude)
);

CREATE TABLE report_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_id BIGINT NOT NULL,
    image_url VARCHAR(512) NOT NULL,
    image_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_images_report FOREIGN KEY (report_id) REFERENCES reports(id)
);

CREATE TABLE status_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_id BIGINT NOT NULL,
    old_status VARCHAR(30) NULL,
    new_status VARCHAR(30) NOT NULL,
    changed_by_user_id BIGINT NOT NULL,
    changed_by_role VARCHAR(40) NOT NULL,
    remarks VARCHAR(1000) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_history_report_created (report_id, created_at),
    CONSTRAINT fk_history_report FOREIGN KEY (report_id) REFERENCES reports(id)
);

CREATE TABLE resolution_evidence (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_id BIGINT NOT NULL UNIQUE,
    resolution_remark VARCHAR(1500) NOT NULL,
    completion_image_url VARCHAR(512) NOT NULL,
    resolved_by_user_id BIGINT NOT NULL,
    resolved_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_resolution_report FOREIGN KEY (report_id) REFERENCES reports(id)
);

CREATE TABLE duplicate_report_links (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_report_id BIGINT NOT NULL,
    duplicate_of_report_id BIGINT NOT NULL,
    distance_meters DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_duplicate_pair (source_report_id, duplicate_of_report_id),
    CONSTRAINT fk_duplicate_source FOREIGN KEY (source_report_id) REFERENCES reports(id),
    CONSTRAINT fk_duplicate_target FOREIGN KEY (duplicate_of_report_id) REFERENCES reports(id)
);

CREATE TABLE outbox_events (
    id CHAR(36) PRIMARY KEY,
    aggregate_id BIGINT NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    payload_json JSON NOT NULL,
    correlation_id CHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    published_at TIMESTAMP(6) NULL,
    INDEX idx_outbox_status_created (status, created_at)
);
