-- 2FA (TOTP) desteği için users tablosuna iki kolon eklenir.
-- Mevcut kullanıcılar için totp_enabled varsayılan FALSE — eski login akışı bozulmaz.
ALTER TABLE users ADD COLUMN totp_secret  VARCHAR(255);
ALTER TABLE users ADD COLUMN totp_enabled BOOLEAN NOT NULL DEFAULT FALSE;
