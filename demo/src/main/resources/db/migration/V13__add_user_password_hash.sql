-- Adds an optional password_hash column to support self-service Sign Up
-- (BCrypt). Existing magic-link users keep the column NULL; magic-link is
-- still the primary authentication path and is not affected.
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);
