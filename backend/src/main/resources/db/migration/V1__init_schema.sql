-- ============================================================
-- V1 — Başlangıç şema migrasyonu
-- Tüm tabloları ve kısıtları oluşturur.
-- Tablo sırası foreign key bağımlılığına göredir.
-- ============================================================

-- ------------------------------------------------------------
-- 1. users
-- Bağımlılık: yok
-- ------------------------------------------------------------
CREATE TABLE users (
    id            BIGSERIAL    PRIMARY KEY,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(50)  NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL,

    CONSTRAINT uk_users_email UNIQUE (email)
);

-- ------------------------------------------------------------
-- 2. sla_rules
-- Bağımlılık: yok
-- ------------------------------------------------------------
CREATE TABLE sla_rules (
    id                    BIGSERIAL   PRIMARY KEY,
    priority              VARCHAR(50) NOT NULL,
    response_time_hours   INTEGER     NOT NULL,
    resolution_time_hours INTEGER     NOT NULL,
    escalation_time_hours INTEGER     NOT NULL,
    active                BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMP   NOT NULL,
    updated_at            TIMESTAMP   NOT NULL,

    CONSTRAINT uk_sla_rules_priority UNIQUE (priority)
);

-- ------------------------------------------------------------
-- 3. tickets
-- Bağımlılık: users
-- ------------------------------------------------------------
CREATE TABLE tickets (
    id             BIGSERIAL    PRIMARY KEY,
    ticket_number  VARCHAR(20)  NOT NULL,
    title          VARCHAR(255) NOT NULL,
    description    TEXT         NOT NULL,
    status         VARCHAR(50)  NOT NULL,
    priority       VARCHAR(50)  NOT NULL,
    created_by_id  BIGINT       NOT NULL,
    assigned_to_id BIGINT,
    due_date       TIMESTAMP,
    resolved_at    TIMESTAMP,
    closed_at      TIMESTAMP,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL,

    CONSTRAINT uk_tickets_ticket_number  UNIQUE      (ticket_number),
    CONSTRAINT fk_tickets_created_by     FOREIGN KEY (created_by_id)  REFERENCES users(id),
    CONSTRAINT fk_tickets_assigned_to    FOREIGN KEY (assigned_to_id) REFERENCES users(id)
);

-- ------------------------------------------------------------
-- 4. comments
-- Bağımlılık: tickets, users
-- ------------------------------------------------------------
CREATE TABLE comments (
    id         BIGSERIAL   PRIMARY KEY,
    content    TEXT        NOT NULL,
    ticket_id  BIGINT      NOT NULL,
    author_id  BIGINT      NOT NULL,
    type       VARCHAR(50) NOT NULL,
    created_at TIMESTAMP   NOT NULL,
    updated_at TIMESTAMP   NOT NULL,

    CONSTRAINT fk_comments_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id),
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users(id)
);

-- ------------------------------------------------------------
-- 5. attachments
-- Bağımlılık: tickets, users
-- ------------------------------------------------------------
CREATE TABLE attachments (
    id               BIGSERIAL    PRIMARY KEY,
    file_name        VARCHAR(255) NOT NULL,
    file_type        VARCHAR(100) NOT NULL,
    file_path        VARCHAR(500) NOT NULL,
    file_size        BIGINT       NOT NULL,
    ticket_id        BIGINT       NOT NULL,
    uploaded_by_id   BIGINT       NOT NULL,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL,

    CONSTRAINT fk_attachments_ticket      FOREIGN KEY (ticket_id)      REFERENCES tickets(id),
    CONSTRAINT fk_attachments_uploaded_by FOREIGN KEY (uploaded_by_id) REFERENCES users(id)
);
