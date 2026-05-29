-- ============================================================
-- V2 — SLA kuralları başlangıç verisi
-- Her öncelik seviyesi için bir SLA kaydı eklenir.
-- Bu veriler olmadan ticket oluşturma işlemi başarısız olur.
-- ============================================================

INSERT INTO sla_rules (priority, response_time_hours, resolution_time_hours, escalation_time_hours, active, created_at, updated_at)
VALUES
    ('BLOCKER',  1,  4,  2,  TRUE, NOW(), NOW()),
    ('CRITICAL', 2,  8,  4,  TRUE, NOW(), NOW()),
    ('HIGH',     4,  24, 12, TRUE, NOW(), NOW()),
    ('MEDIUM',   8,  48, 24, TRUE, NOW(), NOW()),
    ('LOW',      24, 72, 48, TRUE, NOW(), NOW());
