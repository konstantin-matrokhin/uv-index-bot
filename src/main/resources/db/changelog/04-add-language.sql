--liquibase formatted sql
--changeset Konstantin Matrokhin:add-language-enum
create type lang as enum ('ENGLISH', 'RUSSIAN');
create cast (character varying as lang) with inout as implicit;

--changeset Konstantin Matrokhin:add-language-column-to-user
alter table "user" add column language lang not null default 'ENGLISH';
