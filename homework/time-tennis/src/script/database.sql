CREATE TABLE `tt_user`
(
    `open_id`     varchar(64) NOT NULL DEFAULT '',
    `nickname`    varchar(64)          DEFAULT NULL,
    `wx_nickname` varchar(64) NOT NULL,
    `avatar`      varchar(256)         DEFAULT NULL,
    `vip`         tinyint(1)  NOT NULL DEFAULT '0',
    `update_time` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`open_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = UTF8 COMMENT ='用户信息';

CREATE TABLE `tt_booking`
(
    `id`          int(11)     NOT NULL AUTO_INCREMENT,
    `open_id`     varchar(64) NOT NULL,
    `date`        date        NOT NULL,
    `start`       int(11)     NOT NULL,
    `end`         int(11)     NOT NULL,
    `charged`     tinyint(1)  NOT NULL DEFAULT '0',
    `update_time` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_date` (`date`),
    KEY `idx_open_id_date` (`open_id`, `date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = UTF8 COMMENT ='订场记录表';

CREATE TABLE `operation`
(
    `id`          int(11)     NOT NULL AUTO_INCREMENT,
    `operator_id` varchar(64) NOT NULL,
    `description` varchar(256)         DEFAULT NULL,
    `update_time` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_update_time` (`update_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = UTF8l

