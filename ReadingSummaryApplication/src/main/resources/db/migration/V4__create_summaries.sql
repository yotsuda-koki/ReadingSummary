create table summaries (
  id bigserial primary key,
  book_id bigint not null references books(id) on delete cascade,
  scope varchar(20) not null,              -- CHAPTER or BOOK
  chapter int,                              -- scope=CHAPTER の場合に使用（null可）
  content_md text not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index idx_summaries_book_id on summaries(book_id);
create index idx_summaries_book_scope on summaries(book_id, scope);
create index idx_summaries_book_chapter on summaries(book_id, chapter);
