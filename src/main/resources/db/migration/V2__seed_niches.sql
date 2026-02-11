insert into niches (id, slug, name, parent_id, created_at, updated_at)
values
  ('00000000-0000-0000-0000-00000000aa01', 'api', 'API', null, now(), now()),
  ('00000000-0000-0000-0000-00000000aa02', 'databases', 'Databases', null, now(), now()),
  ('00000000-0000-0000-0000-00000000aa03', 'frontend', 'Frontend', null, now(), now())
on conflict (id) do nothing;
