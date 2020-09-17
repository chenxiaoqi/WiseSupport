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

