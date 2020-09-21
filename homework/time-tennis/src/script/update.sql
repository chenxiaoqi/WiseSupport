alter table tt_booking
    add arena_id int default 2 not null after open_id;

alter table tt_booking
    add court_id int default 7 not null after arena_id;

create index idx_date_arena_id
    on tt_booking (date, arena_id);

create index idx_date_arena_id
    on tt_booking (arena_id, date);

create index idx_open_id_date
    on tt_booking (open_id, date);

drop index idx_open_id on membership_card;

create unique index idx_open_id_meta_id
    on membership_card (open_id, meta_id);

alter table membership_card
    modify balance int not null after meta_id;

alter table membership_card
    add expire_date date not null after balance;

alter table membership_card
    drop column status;

create index idx_meta_id
    on membership_card (meta_id);

drop index idx_create_time_status on trade;

drop index idx_open_id on trade;

create index idx_status
    on trade (status);

alter table trade_booking_r
    add court_id int not null;

alter table trade_booking_r
    add start int not null;

alter table trade_booking_r
    add end int not null;

