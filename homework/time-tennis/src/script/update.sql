#2.1.0
alter table arena
    add latitude numeric(10, 6) default 31.990658 not null;

alter table arena
    add longitude numeric(10, 6) default 118.762177 null;

#2.1.1
alter table tt_user
    drop column vip;

alter table tt_user
    drop column admin;

#2.1.3
alter table trade
    drop column mch_id;

alter table trade
    add receiver_id varchar(64) null;

alter table trade
    add receiver_type int null;

alter table trade
    add share_status varchar(4) null;

create index idx_share_status
    on trade (share_status);

alter table arena
    drop column mch_id;

alter table arena
    add receiver_id varchar(64) null;

alter table arena
    add receiver_type int null;

