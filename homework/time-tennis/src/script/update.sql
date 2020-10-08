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
    add date int not null after court_id;

alter table trade_booking_r
    add end int not null;


create index idx_city
    on arena (city);

drop index idx_city on arena;

create index idx_city_type
    on arena (city, type);

alter table arena
    add status varchar(3) default 'ofl' not null;

alter table court
    add status varchar(3) default 'ofl' not null;


alter table tt_booking
    add pay_type varchar(4) null;

alter table tt_booking
    add pay_no varchar(24) null;

alter table tt_booking
    add status varchar(4) default 'ok' not null;

alter table membership_card_bill
    change id bill_no varchar(24) not null;

alter table tt_user
    add phone_number varchar(11) null after wx_nickname;

create index idx_pay_no
    on tt_booking (pay_no);