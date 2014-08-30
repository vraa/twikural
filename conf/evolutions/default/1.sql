# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table data (
  id                        bigint auto_increment not null,
  active_kural_id           integer,
  twitter_followers         varchar(255),
  constraint pk_data primary key (id))
;

create table thirukural (
  id                        bigint auto_increment not null,
  tamil                     varchar(255),
  english                   varchar(255),
  explanation               varchar(255),
  adhikaram                 varchar(255),
  kalaignar                 varchar(255),
  muva                      varchar(255),
  pappaiya                  varchar(255),
  constraint pk_thirukural primary key (id))
;




# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table data;

drop table thirukural;

SET FOREIGN_KEY_CHECKS=1;

