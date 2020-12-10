create table t_user
(
    uid             integer not null constraint t_user_pk primary key autoincrement,
    username        text    not null,
    salt            text    not null,
    password        text    not null,
    nickname        text,
    avatar          text    default 'avatar.jpg',
    status          integer default 0,
    email           text,
    telephone       text,
    lang            text    default 'zh-CN',
    last_login_ip   text,
    last_login_time integer,
    creator_id      integer,
    create_time     integer
);

create table t_notification
(
    id          integer not null constraint t_notification_pk primary key autoincrement,
    content     text,
    status      integer default 0 not null,
    create_date integer
);