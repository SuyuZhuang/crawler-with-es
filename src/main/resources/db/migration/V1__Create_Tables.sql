create table LINKS_ALREADY_PROCESSED( link VARCHAR(2000));

create table LINKS_TO_BE_PROCESSED(link VARCHAR(2000));

create table news(
	id bigint primary key  auto_increment ,
	title TEXT,
	content TEXT,
	url varchar(2000),
	created_at timestamp default now(),
	modified_at timestamp default now()
);