--liquibase formatted sql

--changeset Konstantin Matrokhin:create table user
create table "user"
(
    id         uuid primary key,
    chat_id    bigint    not null,
    name       varchar,
    created_at timestamp not null
);
