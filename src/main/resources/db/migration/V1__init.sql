create table users (
  id uuid primary key,
  email varchar(320) not null unique,
  password_hash varchar(120) not null,
  status varchar(20) not null,
  created_at timestamptz not null,
  updated_at timestamptz not null
);

create table user_roles (
  user_id uuid not null references users(id) on delete cascade,
  role varchar(20) not null,
  primary key (user_id, role)
);

create table seller_profile (
  user_id uuid primary key references users(id) on delete cascade,
  display_name varchar(120) not null,
  bio text,
  payout_info text
);

create table niches (
  id uuid primary key,
  slug varchar(120) not null unique,
  name varchar(120) not null,
  parent_id uuid references niches(id),
  created_at timestamptz not null,
  updated_at timestamptz not null
);

create table courses (
  id uuid primary key,
  seller_id uuid not null references users(id),
  niche_id uuid not null references niches(id),
  title varchar(200) not null,
  description text,
  price numeric(10,2) not null,
  status varchar(20) not null,
  created_at timestamptz not null,
  updated_at timestamptz not null
);

create table notes (
  id uuid primary key,
  course_id uuid references courses(id),
  seller_id uuid not null references users(id),
  niche_id uuid not null references niches(id),
  title varchar(200) not null,
  description text,
  price numeric(10,2),
  file_key varchar(512),
  content_type varchar(120),
  file_size bigint,
  checksum_sha256 varchar(128),
  pages integer,
  status varchar(20) not null,
  created_at timestamptz not null,
  updated_at timestamptz not null
);

create table note_tags (
  note_id uuid not null references notes(id) on delete cascade,
  tag varchar(64) not null,
  primary key (note_id, tag)
);

create table orders (
  id uuid primary key,
  client_id uuid not null references users(id),
  status varchar(20) not null,
  total_amount numeric(10,2) not null,
  currency varchar(10) not null,
  created_at timestamptz not null,
  updated_at timestamptz not null
);

create table order_items (
  id uuid primary key,
  order_id uuid not null references orders(id) on delete cascade,
  item_type varchar(20) not null,
  item_id uuid not null,
  unit_price numeric(10,2) not null,
  created_at timestamptz not null,
  updated_at timestamptz not null
);

create table payments (
  id uuid primary key,
  order_id uuid not null references orders(id) on delete cascade,
  provider varchar(20) not null,
  status varchar(20) not null,
  provider_ref varchar(120),
  amount numeric(10,2) not null,
  created_at timestamptz not null,
  updated_at timestamptz not null
);

create table entitlements (
  id uuid primary key,
  client_id uuid not null references users(id) on delete cascade,
  item_type varchar(20) not null,
  item_id uuid not null,
  granted_at timestamptz not null,
  created_at timestamptz not null,
  updated_at timestamptz not null,
  unique (client_id, item_type, item_id)
);

create index idx_notes_niche on notes(niche_id);
create index idx_notes_seller on notes(seller_id);
create index idx_notes_status on notes(status);
create index idx_notes_created_at on notes(created_at);
create index idx_note_tags_tag on note_tags(tag);

create index idx_courses_niche on courses(niche_id);
create index idx_courses_seller on courses(seller_id);
create index idx_courses_status on courses(status);
create index idx_courses_created_at on courses(created_at);

create index idx_orders_client on orders(client_id);
create index idx_orders_status on orders(status);
create index idx_orders_created_at on orders(created_at);

create index idx_order_items_order on order_items(order_id);
create index idx_payments_order on payments(order_id);

create index idx_entitlements_client on entitlements(client_id);