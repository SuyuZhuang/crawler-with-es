create table LINKS_ALREADY_PROCESSED( link VARCHAR(2000));

create table LINKS_TO_BE_PROCESSED(link VARCHAR(2000));


CREATE SEQUENCE news_id_seq MINVALUE 1 START 1;

create table news(
	id BIGINT DEFAULT nextval('news_id_seq') PRIMARY KEY ,
	title TEXT,
	content TEXT,
	url varchar(2000),
	created_at timestamp default now(),
	modified_at timestamp default now(),
	uuid varchar(36),
	snow bigint
);