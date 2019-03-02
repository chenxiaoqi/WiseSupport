create table USER
(
  ID       INTEGER auto_increment,
  ACCOUNT  VARCHAR(32)
    constraint USER_ACCOUNT_UINDEX
      unique,
  PASSWORD VARCHAR(32),
  LOCALE   VARCHAR(16),
  constraint USER_PK
    primary key (ID)
);

