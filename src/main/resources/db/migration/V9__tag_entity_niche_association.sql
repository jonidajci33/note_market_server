SET search_path TO note_seller;

-- STEP 1: Create tags table (BaseEntity pattern: UUID PK, slug unique, name, timestamps)
CREATE TABLE tags (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    slug       VARCHAR(120) NOT NULL,
    name       VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT tags_pkey    PRIMARY KEY (id),
    CONSTRAINT tags_slug_uk UNIQUE (slug)
);

-- STEP 2: Migrate existing string tags -> tag entity rows (dedup + slugify)
INSERT INTO tags (id, slug, name, created_at, updated_at)
SELECT
    gen_random_uuid(),
    REGEXP_REPLACE(REGEXP_REPLACE(LOWER(TRIM(tag)), '\s+', '-', 'g'), '[^a-z0-9\-]', '', 'g'),
    INITCAP(TRIM(tag)),
    now(), now()
FROM (SELECT DISTINCT tag FROM note_tags WHERE tag IS NOT NULL AND TRIM(tag) <> '') dt
ON CONFLICT (slug) DO NOTHING;

-- STEP 3: Capture existing note->tag associations before DROP
CREATE TEMP TABLE _note_tag_migration AS
SELECT nt.note_id, t.id AS tag_id
FROM note_tags nt
JOIN tags t ON t.slug = REGEXP_REPLACE(REGEXP_REPLACE(LOWER(TRIM(nt.tag)), '\s+', '-', 'g'), '[^a-z0-9\-]', '', 'g')
WHERE nt.note_id IN (SELECT id FROM notes);

-- STEP 4: Drop old note_tags (ElementCollection artifact)
DROP TABLE note_tags;

-- STEP 5: Recreate note_tags as normalized FK join table
CREATE TABLE note_tags (
    note_id UUID NOT NULL,
    tag_id  UUID NOT NULL,
    CONSTRAINT note_tags_pkey    PRIMARY KEY (note_id, tag_id),
    CONSTRAINT note_tags_note_fk FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE,
    CONSTRAINT note_tags_tag_fk  FOREIGN KEY (tag_id)  REFERENCES tags(id)  ON DELETE RESTRICT
);

-- STEP 6: Re-insert migrated associations
INSERT INTO note_tags (note_id, tag_id) SELECT note_id, tag_id FROM _note_tag_migration ON CONFLICT DO NOTHING;
DROP TABLE IF EXISTS _note_tag_migration;

-- STEP 7: Create niche_tags join table
CREATE TABLE niche_tags (
    niche_id UUID NOT NULL,
    tag_id   UUID NOT NULL,
    CONSTRAINT niche_tags_pkey     PRIMARY KEY (niche_id, tag_id),
    CONSTRAINT niche_tags_niche_fk FOREIGN KEY (niche_id) REFERENCES niches(id) ON DELETE CASCADE,
    CONSTRAINT niche_tags_tag_fk   FOREIGN KEY (tag_id)   REFERENCES tags(id)   ON DELETE CASCADE
);

-- STEP 8: Indexes
CREATE INDEX idx_tags_slug         ON tags(slug);
CREATE INDEX idx_tags_name         ON tags(name);
CREATE INDEX idx_note_tags_tag_id  ON note_tags(tag_id);
CREATE INDEX idx_niche_tags_tag_id ON niche_tags(tag_id);
CREATE INDEX idx_niche_tags_niche_id ON niche_tags(niche_id);
