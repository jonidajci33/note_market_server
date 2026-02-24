-- Local profile demo seed data for quick manual testing.
-- Loaded only when spring.flyway.locations includes classpath:db/local.

insert into users (id, email, password_hash, status, created_at, updated_at)
values
  ('00000000-0000-0000-0000-000000000101', 'seller.alpha@notes.local', 'demo-hash', 'ACTIVE', now() - interval '10 days', now() - interval '1 day'),
  ('00000000-0000-0000-0000-000000000102', 'seller.beta@notes.local', 'demo-hash', 'ACTIVE', now() - interval '9 days', now() - interval '1 day')
on conflict (email) do nothing;

insert into user_roles (user_id, role)
values
  ('00000000-0000-0000-0000-000000000101', 'SELLER'),
  ('00000000-0000-0000-0000-000000000102', 'SELLER')
on conflict (user_id, role) do nothing;

insert into seller_profile (user_id, display_name, bio, payout_info)
values
  ('00000000-0000-0000-0000-000000000101', 'Alpha Notes', 'Engineering and productivity note packs.', 'demo'),
  ('00000000-0000-0000-0000-000000000102', 'Beta Study Lab', 'Computer science and interview prep notes.', 'demo')
on conflict (user_id) do nothing;

insert into niches (id, slug, name, category_id, created_at, updated_at)
values
  ('00000000-0000-0000-0000-000000000201', 'technology', 'Technology', '00000000-0000-0000-0000-00000000bb01', now() - interval '12 days', now() - interval '2 days'),
  ('00000000-0000-0000-0000-000000000202', 'software-engineering', 'Software Engineering', '00000000-0000-0000-0000-00000000bb01', now() - interval '11 days', now() - interval '2 days'),
  ('00000000-0000-0000-0000-000000000203', 'machine-learning', 'Machine Learning', '00000000-0000-0000-0000-00000000bb01', now() - interval '11 days', now() - interval '2 days')
on conflict (slug) do nothing;

insert into courses (id, seller_id, niche_id, title, description, price, status, created_at, updated_at)
values
  ('00000000-0000-0000-0000-000000000301', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000202', 'Spring Boot API Engineering', 'Course with practical backend architecture patterns.', 49.99, 'PUBLISHED', now() - interval '8 days', now() - interval '1 day'),
  ('00000000-0000-0000-0000-000000000302', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000203', 'Applied ML System Design', 'Production-minded machine learning notes and checklists.', 59.99, 'PUBLISHED', now() - interval '7 days', now() - interval '1 day')
on conflict (id) do nothing;

insert into notes (id, course_id, seller_id, niche_id, title, description, price, file_key, content_type, file_size, checksum_sha256, pages, status, created_at, updated_at)
values
  ('00000000-0000-0000-0000-000000000401', '00000000-0000-0000-0000-000000000301', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000202',
   'REST API Security Checklist', 'JWT, RBAC, CORS, and secure error-handling checklist for Spring services.', 12.99, 'demo/rest-api-security-checklist.pdf',
   'application/pdf', 245760, 'demo-checksum-401', 24, 'PUBLISHED', now() - interval '6 days', now() - interval '1 day'),
  ('00000000-0000-0000-0000-000000000402', '00000000-0000-0000-0000-000000000301', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000202',
   'PostgreSQL Query Tuning Notes', 'Practical indexing, EXPLAIN workflow, and query anti-patterns.', 15.50, 'demo/postgres-query-tuning.pdf',
   'application/pdf', 327680, 'demo-checksum-402', 31, 'PUBLISHED', now() - interval '5 days', now() - interval '1 day'),
  ('00000000-0000-0000-0000-000000000403', '00000000-0000-0000-0000-000000000302', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000203',
   'Feature Store Design Cheatsheet', 'Data freshness, offline/online parity, and ownership guardrails.', 18.00, 'demo/feature-store-design.pdf',
   'application/pdf', 286720, 'demo-checksum-403', 28, 'PUBLISHED', now() - interval '4 days', now() - interval '1 day'),
  ('00000000-0000-0000-0000-000000000404', '00000000-0000-0000-0000-000000000302', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000203',
   'MLOps Deployment Runbook', 'CI/CD, model rollout strategies, and incident response playbook.', 21.00, 'demo/mlops-deployment-runbook.pdf',
   'application/pdf', 409600, 'demo-checksum-404', 36, 'PUBLISHED', now() - interval '3 days', now() - interval '1 day')
on conflict (id) do nothing;

insert into note_tags (note_id, tag)
values
  ('00000000-0000-0000-0000-000000000401', 'spring'),
  ('00000000-0000-0000-0000-000000000401', 'security'),
  ('00000000-0000-0000-0000-000000000401', 'api'),
  ('00000000-0000-0000-0000-000000000402', 'postgresql'),
  ('00000000-0000-0000-0000-000000000402', 'performance'),
  ('00000000-0000-0000-0000-000000000402', 'database'),
  ('00000000-0000-0000-0000-000000000403', 'ml'),
  ('00000000-0000-0000-0000-000000000403', 'architecture'),
  ('00000000-0000-0000-0000-000000000404', 'mlops'),
  ('00000000-0000-0000-0000-000000000404', 'deployment')
on conflict (note_id, tag) do nothing;
