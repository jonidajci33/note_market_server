create table note_ratings (
  id          uuid          primary key,
  note_id     uuid          not null references notes(id) on delete cascade,
  user_id     uuid          not null references users(id) on delete cascade,
  rating      integer       not null,
  review_text text,
  created_at  timestamptz   not null,
  updated_at  timestamptz   not null,

  constraint uq_note_ratings_user_note unique (user_id, note_id),
  constraint chk_note_ratings_rating check (rating between 1 and 5)
);

create index idx_note_ratings_note_rating
  on note_ratings (note_id, rating);

create index idx_note_ratings_note_created
  on note_ratings (note_id, created_at desc);

create index idx_note_ratings_user_created
  on note_ratings (user_id, created_at desc);
