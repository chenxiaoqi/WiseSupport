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