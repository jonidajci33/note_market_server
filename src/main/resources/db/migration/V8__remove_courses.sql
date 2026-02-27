-- V8: Remove all course-related database structures
-- Courses feature has been removed. Notes are organized by niche and category only.
-- Historic COURSE rows in order_items and entitlements are intentionally preserved
-- for backward compatibility with existing order data.

SET search_path TO note_seller;

-- Drop FK constraint from notes.course_id
ALTER TABLE notes DROP CONSTRAINT IF EXISTS notes_course_id_fkey;

-- Drop course_id column from notes
ALTER TABLE notes DROP COLUMN IF EXISTS course_id;

-- Drop course indexes
DROP INDEX IF EXISTS idx_courses_niche;
DROP INDEX IF EXISTS idx_courses_seller;
DROP INDEX IF EXISTS idx_courses_status;
DROP INDEX IF EXISTS idx_courses_created_at;

-- Drop courses table
DROP TABLE IF EXISTS courses;
