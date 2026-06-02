-- ============================================================
-- V4 — notifications tablosu
-- Kullanıcılara iletilen bildirimleri depolar.
-- FK: user_id → users(id)
-- reference_id için FK yok; ticket silinse bile bildirim korunur.
-- ============================================================

CREATE TABLE notifications (
    id           BIGSERIAL    PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    type         VARCHAR(100) NOT NULL,
    message      TEXT         NOT NULL,
    reference_id BIGINT,
    seen         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL,

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Okunmamış bildirimleri kullanıcıya göre hızlı sorgulamak için composite index
CREATE INDEX idx_notifications_user_seen
    ON notifications(user_id, seen);
