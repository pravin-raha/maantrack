create table users (
    users_id bigserial primary key,
    name varchar(100) not null,
    age int,
    username varchar(100) not null unique,
    role varchar(25),
    password varchar,
    email varchar
);


create table if not exists token (
	token_id bigserial primary key,
	secure_id bytea not null,
	users_id bigint not null references users on delete cascade,
	expiry timestamp with time zone not null,
	last_touched timestamp with time zone
);
