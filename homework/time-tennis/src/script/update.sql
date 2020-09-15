alter table tt_booking
    add arena_id int default 2 not null after open_id;

alter table tt_booking
    add court_id int default 7 not null after arena_id;

create index idx_date_arena_id
    on tt_booking (arena_id, date);
