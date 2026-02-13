-- Create categories table
create table categories (
  id uuid primary key,
  slug varchar(120) not null unique,
  name varchar(120) not null,
  created_at timestamptz not null,
  updated_at timestamptz not null
);

create index idx_categories_slug on categories(slug);

-- Seed initial categories
insert into categories (id, slug, name, created_at, updated_at) values
  ('00000000-0000-0000-0000-00000000bb01', 'technology', 'Technology', now(), now()),
  ('00000000-0000-0000-0000-00000000bb02', 'science', 'Science', now(), now()),
  ('00000000-0000-0000-0000-00000000bb03', 'business', 'Business', now(), now())
on conflict (id) do nothing;

-- Add category_id to niches
alter table niches add column category_id uuid references categories(id);
create index idx_niches_category on niches(category_id);

-- Backfill existing niches under Technology
update niches set category_id = '00000000-0000-0000-0000-00000000bb01' where category_id is null;

-- Make category_id required
alter table niches alter column category_id set not null;

-- Drop unused parent_id self-reference
alter table niches drop column parent_id;
