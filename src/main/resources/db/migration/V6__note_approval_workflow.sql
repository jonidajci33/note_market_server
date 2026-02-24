-- Add approval workflow columns to notes table
ALTER TABLE notes
    ADD COLUMN rejection_reason  TEXT,
    ADD COLUMN submission_count  INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN reviewed_by       UUID    REFERENCES users(id) ON DELETE SET NULL,
    ADD COLUMN reviewed_at       TIMESTAMPTZ;

-- Enforce valid status values at DB level
ALTER TABLE notes
    ADD CONSTRAINT chk_notes_status
        CHECK (status IN ('DRAFT','PENDING_APPROVAL','PUBLISHED','REJECTED','ARCHIVED'));

-- Enforce: REJECTED notes must have a rejection reason
ALTER TABLE notes
    ADD CONSTRAINT chk_notes_rejection_reason_required
        CHECK (status != 'REJECTED' OR rejection_reason IS NOT NULL);

-- Enforce: submission count bounded 1-4 (initial + max 3 resubmissions)
ALTER TABLE notes
    ADD CONSTRAINT chk_notes_submission_count_range
        CHECK (submission_count BETWEEN 1 AND 4);

-- Partial index for admin moderation queue
CREATE INDEX idx_notes_pending_approval
    ON notes (created_at DESC)
    WHERE status = 'PENDING_APPROVAL';

-- Partial index for admin audit lookups
CREATE INDEX idx_notes_reviewed_by
    ON notes (reviewed_by)
    WHERE reviewed_by IS NOT NULL;
