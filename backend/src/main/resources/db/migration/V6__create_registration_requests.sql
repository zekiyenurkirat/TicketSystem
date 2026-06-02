-- ============================================================
-- V6 — registration_requests tablosu
-- CUSTOMER'ların sisteme kayıt başvurularını depolar.
-- Manager onayı olmadan kullanıcı hesabı oluşturulmaz.
-- FK: reviewed_by_id → users(id)  (nullable)
-- ============================================================

CREATE TABLE registration_requests (
    id               BIGSERIAL    PRIMARY KEY,
    first_name       VARCHAR(100) NOT NULL,
    last_name        VARCHAR(100) NOT NULL,
    email            VARCHAR(255) NOT NULL,
    password_hash    VARCHAR(255) NOT NULL,
    requested_role   VARCHAR(50)  NOT NULL DEFAULT 'CUSTOMER',
    status           VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    note             TEXT,
    reviewed_by_id   BIGINT,
    reviewed_at      TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL,

    CONSTRAINT fk_rreq_reviewed_by
        FOREIGN KEY (reviewed_by_id) REFERENCES users(id)
);

CREATE INDEX idx_rreq_status ON registration_requests(status);
CREATE INDEX idx_rreq_email  ON registration_requests(email);
