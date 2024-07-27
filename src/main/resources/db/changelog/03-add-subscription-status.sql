--liquibase formatted sql

--changeset Konstantin Matrokhin:add subscription status
alter table "user" add column is_subscribed boolean default false not null;
