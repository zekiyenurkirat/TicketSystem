-- jBPM process instance kimliğini tickets tablosuna ekler.
-- Nullable: mevcut kayıtlar NULL olarak kalır.
-- Yeni oluşturulan ticketlar, jBPM process başlatılınca bu kolona dolar.
ALTER TABLE tickets
    ADD COLUMN process_instance_id VARCHAR(255);
