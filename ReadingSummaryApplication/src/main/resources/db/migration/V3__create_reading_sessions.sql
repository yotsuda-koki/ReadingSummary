create table reading_sessions (
  id bigserial primary key,
  book_id bigint not null references books(id) on delete cascade,
  session_date date not null,
  minutes int not null,
  pages_read int,
  memo text,
  created_at timestamptz not null default now()
);

create index idx_reading_sessions_book_id on reading_sessions(book_id);
create index idx_reading_sessions_book_date on reading_sessions(book_id, session_date desc);
