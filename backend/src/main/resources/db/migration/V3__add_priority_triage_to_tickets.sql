-- ============================================================
-- V3 — Priority Triage System alanları
-- tickets tablosuna customerPriority, impact, urgency,
-- suggestedPriority ve priority review alanları eklenir.
-- Tüm yeni kolonlar nullable olarak eklenir.
-- ============================================================

-- ------------------------------------------------------------
-- Yeni kolonlar eklenir (tümü nullable)
-- ------------------------------------------------------------
ALTER TABLE tickets ADD COLUMN customer_priority       VARCHAR(50);
ALTER TABLE tickets ADD COLUMN impact                  VARCHAR(50);
ALTER TABLE tickets ADD COLUMN urgency                 VARCHAR(50);
ALTER TABLE tickets ADD COLUMN suggested_priority      VARCHAR(50);
ALTER TABLE tickets ADD COLUMN priority_review_note    TEXT;
ALTER TABLE tickets ADD COLUMN priority_reviewed_at    TIMESTAMP;
ALTER TABLE tickets ADD COLUMN priority_reviewed_by_id BIGINT;

-- ------------------------------------------------------------
-- Mevcut kayıtlar için backfill
-- customer_priority ve suggested_priority, mevcut aktif
-- priority değeriyle set edilir.
-- impact ve urgency bilinmediğinden NULL bırakılır.
-- ------------------------------------------------------------
UPDATE tickets
SET customer_priority = priority
WHERE customer_priority IS NULL;

UPDATE tickets
SET suggested_priority = priority
WHERE suggested_priority IS NULL;

-- ------------------------------------------------------------
-- Foreign key constraint
-- Kolon ekleme ve backfill tamamlandıktan sonra tanımlanır.
-- ------------------------------------------------------------
ALTER TABLE tickets
    ADD CONSTRAINT fk_tickets_priority_reviewed_by
    FOREIGN KEY (priority_reviewed_by_id)
    REFERENCES users(id);
