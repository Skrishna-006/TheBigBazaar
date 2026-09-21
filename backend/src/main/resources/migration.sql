-- Idempotent migration to add OAuth2 columns to the existing 'users' table.
-- ** IMPORTANT MANUAL MIGRATION **
-- Since this project does not use Flyway, this script MUST be executed manually against
-- the target PostgreSQL database before or during deployment of the Google OAuth version.
-- Safely applies default values to existing rows without data loss.

DO $$
BEGIN
    -- 1. Add auth_provider if missing
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'auth_provider') THEN
        -- Adds column, applies 'LOCAL' to existing rows, and enforces NOT NULL
        ALTER TABLE users ADD COLUMN auth_provider VARCHAR(20) DEFAULT 'LOCAL' NOT NULL;
    END IF;

    -- 2. Add google_subject if missing
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'google_subject') THEN
        -- Adds column and applies UNIQUE constraint. PostgreSQL naturally allows multiple NULL values in a UNIQUE column.
        ALTER TABLE users ADD COLUMN google_subject VARCHAR(255) UNIQUE;
    END IF;
END $$;
