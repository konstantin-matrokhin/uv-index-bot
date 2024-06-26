--liquibase formatted sql

--changeset Konstantin Matrokhin:create table location
create table location
(
    id            uuid primary key,
    latitude      double precision       not null,
    longitude     double precision       not null,
    user_id       uuid references "user" not null,
    name          varchar,
    last_uv_index real                   not null,
    created_at    timestamp              not null
);
