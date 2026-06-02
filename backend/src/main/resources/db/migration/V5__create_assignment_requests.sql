-- ============================================================
-- V5 — assignment_requests tablosu
-- Agent'ların atanmamış ticket'lar için oluşturduğu istekleri depolar.
-- FK: ticket_id → tickets(id)
--     requested_by_id → users(id)
--     reviewed_by_id  → users(id)  (nullable)
-- ============================================================

CREATE TABLE assignment_requests (
    id               BIGSERIAL    PRIMARY KEY,
    ticket_id        BIGINT       NOT NULL,
    requested_by_id  BIGINT       NOT NULL,
    status           VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    note             TEXT,
    reviewed_by_id   BIGINT,
    reviewed_at      TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL,

    CONSTRAINT fk_areq_ticket
        FOREIGN KEY (ticket_id)       REFERENCES tickets(id),
    CONSTRAINT fk_areq_requested_by
        FOREIGN KEY (requested_by_id) REFERENCES users(id),
    CONSTRAINT fk_areq_reviewed_by
        FOREIGN KEY (reviewed_by_id)  REFERENCES users(id)
);

CREATE INDEX idx_areq_status    ON assignment_requests(status);
CREATE INDEX idx_areq_ticket    ON assignment_requests(ticket_id);
CREATE INDEX idx_areq_requested ON assignment_requests(requested_by_id);
