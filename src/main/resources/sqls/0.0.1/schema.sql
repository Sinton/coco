create table if not exists t_endpoint
(
    id               integer not null constraint t_endpoints_pk primary key autoincrement,
    name             text    not null,
    port             integer default 2375 not null,
    public_ip        text,
    endpoint_url     text,
    tls_enable       integer default 0,
    resources        text,
    docker_config    text,
    status           integer default 0,
    update_date_time integer,
    tls_config       text,
    endpoint_type    integer default 1,
    owner            integer
);

create unique index if not exists t_endpoint_id_uindex on t_endpoint (id);

create table if not exists t_notification
(
    id          integer not null constraint t_notification_pk primary key autoincrement,
    content     text,
    status      integer default 0 not null,
    create_date integer
);

create table if not exists t_setting
(
    id    text not null constraint t_setting_pk primary key,
    key   text,
    value text,
    owner integer
);

create table if not exists t_stack
(
    id           integer not null constraint t_stack_pk primary key autoincrement,
    name         text default '' not null,
    status       integer default '0' not null,
    type         integer default 0,
    endpoint     text,
    swarm_id     text,
    project_path text,
    owner        integer,
    internal     integer default 0
);

create table if not exists t_user
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
    create_time     integer
);
