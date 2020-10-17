#默认只要设置好哦
alter table tt_booking
    add arena_id int default 1 not null after open_id;

alter table tt_booking
    add court_id int default 1 not null after arena_id;

create index idx_date_arena_id
    on tt_booking (date, arena_id);

create index idx_open_id_date
    on tt_booking (open_id, date);

alter table tt_booking
    add pay_type varchar(4) default 'mc' null;

alter table tt_booking
    add pay_no varchar(24) null;

alter table tt_booking
    add status varchar(4) default 'ok' not null;

alter table tt_user
    add phone_number varchar(11) null after wx_nickname;

create index idx_pay_no
    on tt_booking (pay_no);

CREATE TABLE `arena`
(
    `id`                   int(11)     NOT NULL AUTO_INCREMENT,
    `name`                 varchar(18) NOT NULL,
    `type`                 int(11)     NOT NULL,
    `address`              varchar(64) NOT NULL,
    `province`             varchar(8)  NOT NULL,
    `city`                 varchar(8)  NOT NULL,
    `district`             varchar(8)  NOT NULL,
    `phone`                varchar(16) NOT NULL,
    `images`               varchar(96)          DEFAULT NULL,
    `introduction`         varchar(128)         DEFAULT NULL,
    `advance_book_days`    int(11)     NOT NULL DEFAULT '7',
    `book_start_hour`      int(11)     NOT NULL,
    `book_end_hour`        int(11)     NOT NULL,
    `book_style`           int(11)     NOT NULL DEFAULT '2',
    `create_time`          timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status`               varchar(4)  NOT NULL DEFAULT 'sofl',
    `mch_id`               varchar(24)          DEFAULT NULL,
    `allow_half_hour`      tinyint(1)  NOT NULL DEFAULT '0',
    `book_at_least`        tinyint(4)  NOT NULL DEFAULT '1',
    `refund_advance_hours` tinyint(4)  NOT NULL DEFAULT '24',
    `refund_times_limit`   tinyint(4)  NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_city_type` (`city`, `type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='场馆';

CREATE TABLE `arena_favorite`
(
    `open_id`  varchar(64) NOT NULL,
    `arena_id` int(11)     NOT NULL,
    PRIMARY KEY (`open_id`, `arena_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `arena_role`
(
    `arena_id` int(11)     NOT NULL,
    `open_id`  varchar(64) NOT NULL,
    `role`     varchar(8)  NOT NULL,
    PRIMARY KEY (`open_id`, `arena_id`, `role`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `court`
(
    `id`       int(11)     NOT NULL AUTO_INCREMENT,
    `arena_id` int(11)     NOT NULL,
    `name`     varchar(16) NOT NULL,
    `fee`      int(11)     NOT NULL,
    `status`   varchar(3)  NOT NULL DEFAULT 'ofl',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `court_rule_r`
(
    `court_id` int(11) NOT NULL,
    `rule_id`  int(11) NOT NULL,
    `seq`      int(11) NOT NULL,
    PRIMARY KEY (`court_id`, `rule_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `membership_card`
(
    `code`        varchar(16) CHARACTER SET latin1 NOT NULL,
    `open_id`     varchar(64) CHARACTER SET latin1 NOT NULL,
    `meta_id`     int(11)                                   DEFAULT NULL,
    `balance`     int(11)                          NOT NULL,
    `expire_date` date                             NOT NULL,
    `create_time` timestamp                        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`code`),
    UNIQUE KEY `idx_meta_id_open_id` (`meta_id`, `open_id`),
    KEY `idx_meta_id` (`meta_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `membership_card_bill`
(
    `bill_no`      varchar(24) NOT NULL,
    `open_id`      varchar(64) NOT NULL,
    `code`         varchar(16) NOT NULL,
    `product_type` varchar(4)  NOT NULL,
    `fee`          int(11)     NOT NULL,
    `balance`      int(11)     NOT NULL,
    `create_time`  timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`bill_no`),
    KEY `idx_o_m_c` (`open_id`, `code`, `create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `membership_card_meta`
(
    `id`              int(11)     NOT NULL AUTO_INCREMENT,
    `name`            varchar(32) NOT NULL,
    `initial_balance` int(11)     NOT NULL,
    `discount`        int(11)     NOT NULL,
    `price`           int(11)     NOT NULL,
    `status`          varchar(4)  NOT NULL DEFAULT 'ofl',
    `extend_month`    int(11)     NOT NULL,
    `arena_id`        int(11)     NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_arena_id` (`arena_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `rule`
(
    `id`         int(11)     NOT NULL AUTO_INCREMENT,
    `arena_id`   int(11)     NOT NULL,
    `name`       varchar(32) NOT NULL,
    `type`       int(11)     NOT NULL DEFAULT '1',
    `start_date` varchar(10)          DEFAULT NULL,
    `end_date`   varchar(10)          DEFAULT NULL,
    `week`       int(11)              DEFAULT NULL,
    `start_hour` int(11)              DEFAULT NULL,
    `end_hour`   int(11)              DEFAULT NULL,
    `fee`        int(11)              DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `trade`
(
    `trade_no`       varchar(24) NOT NULL,
    `open_id`        varchar(64) NOT NULL,
    `mch_id`         varchar(32) NOT NULL,
    `product_type`   varchar(2)  NOT NULL,
    `status`         varchar(4)  NOT NULL DEFAULT 'wp',
    `prepare_id`     varchar(64) NOT NULL,
    `transaction_id` varchar(32)          DEFAULT NULL,
    `fee`            int(11)     NOT NULL,
    `create_time`    timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`trade_no`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `trade_membership_card_meta_r`
(
    `trade_no` varchar(24) NOT NULL,
    `meta_id`  int(11) DEFAULT NULL,
    PRIMARY KEY (`trade_no`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `trade_membership_card_recharge_r`
(
    `trade_no` varchar(24) NOT NULL,
    `mc_code`  varchar(16) NOT NULL,
    `finished` tinyint(4)  NOT NULL DEFAULT '0',
    PRIMARY KEY (`trade_no`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `user_role`
(
    `open_id`   varchar(64) NOT NULL,
    `role_name` varchar(16) NOT NULL,
    PRIMARY KEY (`open_id`, `role_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

#=================================
alter table booking_share
    add pay_no varchar(24) null;

alter table arena
    add charge_strategy tinyint default 0 not null;

alter table arena
    add book_hours_limit tinyint default 0 not null;

ALTER TABLE tt_user
    CHANGE wx_nickname wx_nickname VARCHAR(64) CHARACTER SET utf8mb4;

alter table monthly_stat
    add cancel_times int null;

alter table monthly_stat
    alter column open_id drop default;

alter table monthly_stat
    alter column month drop default;

alter table monthly_stat
    modify fee int null;

alter table monthly_stat
    modify hours int null;

alter table monthly_stat
    modify balance int null;