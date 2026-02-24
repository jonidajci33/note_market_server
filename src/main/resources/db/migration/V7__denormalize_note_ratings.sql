-- ============================================================
-- V7__denormalize_note_ratings.sql
--
-- Adds denormalized rating summary columns to notes table
-- and backfills from existing note_ratings data.
-- ============================================================

-- Step 1: Add columns
ALTER TABLE notes
    ADD COLUMN average_rating  DOUBLE PRECISION,
    ADD COLUMN rating_count    INTEGER NOT NULL DEFAULT 0;

-- Step 2: Backfill from existing ratings
UPDATE notes n
SET
    average_rating = sub.avg_rating,
    rating_count   = sub.cnt
FROM (
    SELECT
        note_id,
        AVG(rating)::DOUBLE PRECISION AS avg_rating,
        COUNT(*)::INTEGER             AS cnt
    FROM note_ratings
    GROUP BY note_id
) sub
WHERE n.id = sub.note_id;

-- Step 3: Index for sorting/filtering by rating
-- Partial index excludes unrated notes (average_rating IS NULL)
-- Supports "sort by highest rated" and "minimum rating" filter queries
CREATE INDEX idx_notes_avg_rating
    ON notes (average_rating DESC)
    WHERE average_rating IS NOT NULL;
