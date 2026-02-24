-- Pre-create the note_seller schema before the application starts.
-- Required because Flyway 10+ community edition no longer supports
-- automatic schema creation (createSchemas is a Teams-only feature).
-- This script runs once during Postgres first-time initialization
-- (when the data volume is empty).

CREATE SCHEMA IF NOT EXISTS note_seller;

-- Set default search_path so every connection (including Flyway)
-- resolves unqualified names to note_seller automatically.
ALTER DATABASE note_seller SET search_path TO note_seller, public;
