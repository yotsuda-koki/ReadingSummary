create table books (
  id bigserial primary key,
  user_id bigint not null references users(id),
  title varchar(255) not null,
  author varchar(255),
  language varchar(50),
  level varchar(50),
  status varchar(30) not null,
  total_pages int,
  current_page int not null default 0,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index idx_books_user_id on books(user_id);
