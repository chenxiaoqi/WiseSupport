create table booking_bill
(
    open_id    varchar(64) default '' not null,
    booking_id int                    not null,
    fee        int                    not null,
    share      tinyint     default 0  not null,
    date       date                   not null,
    start      int                    not null,
    end        int                    not null,
    primary key (open_id, booking_id)
)
    charset = utf8;

create table booking_share
(
    booking_id  int                                 not null,
    open_id     varchar(64)                         null,
    update_time timestamp default CURRENT_TIMESTAMP not null,
    constraint idx_booking_id_open_id
        unique (booking_id, open_id)
)
    comment '订场费用分摊';

create table charge_history
(
    id          int auto_increment
        primary key,
    open_id     varchar(64)                         not null,
    fee         int                                 not null,
    memo        varchar(64)                         null,
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
)
    comment '充值记录表' charset = utf8;

create index idx_open_id
    on charge_history (open_id, update_time);

create table monthly_stat
(
    month      date        default '0000-00-00' not null,
    open_id    varchar(64) default ''           not null,
    fee        int                              not null,
    hours      int                              not null,
    book_times int                              null,
    balance    int         default 0            not null,
    primary key (month, open_id)
)
    charset = utf8;

create table operation
(
    id             int auto_increment
        primary key,
    operator_id    varchar(64)                         not null,
    description    varchar(256)                        null,
    update_time    timestamp default CURRENT_TIMESTAMP not null,
    operation_type varchar(3)                          not null
)
    charset = utf8;

create index idx_update_time
    on operation (update_time);

create table tt_booking
(
    id          int auto_increment
        primary key,
    open_id     varchar(64)                          not null,
    date        date                                 not null,
    start       int                                  not null,
    end         int                                  not null,
    fee         int        default 0                 not null,
    charged     tinyint(1) default 0                 not null,
    update_time timestamp  default CURRENT_TIMESTAMP not null
)
    comment '订场记录表' charset = utf8;

create index idx_open_id_id
    on tt_booking (open_id, id);

create index idx_update_time
    on tt_booking (update_time);

create table tt_user
(
    open_id     varchar(64) default ''                not null
        primary key,
    nickname    varchar(64)                           null,
    wx_nickname varchar(64)                           not null,
    avatar      varchar(256)                          null,
    vip         tinyint(1)  default 0                 not null,
    update_time timestamp   default CURRENT_TIMESTAMP not null,
    admin       tinyint(1)  default 0                 not null,
    balance     int         default 0                 not null
)
    comment '用户信息' charset = utf8;