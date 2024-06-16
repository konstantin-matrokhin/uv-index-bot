--liquibase formatted sql

--changeset Konstantin Matrokhin:create table location
create table location
(
    id        uuid primary key,
    user_id   uuid references "user" not null,
    name      varchar                not null,
    latitude  float                  not null,
    longitude float                  not null
);
