create table  if not exists user (
    user_id bigserial primary key,
    avatar_url varchar,
    avatar_source varchar,
    bio text,
    confirmed boolean,
    email varchar not null,
    first_name varchar,
    last_name varchar,
    user_type varchar,
    profile_url varchar,
    password varchar,
    birth_date timestamp,
    user_name varchar unique not null,
    created_date timestamp not null,
    modified_date timestamp not null
);

create table if not exists token (
	token_id bigserial primary key,
	secure_id bytea not null,
	user_id bigint not null references user on delete cascade,
	expiry timestamp with time zone not null,
	last_touched timestamp with time zone
);

create table organization(
    organization_id bigserial primary key,
    description text,
    display_name varchar,
    name varchar,
    organization_url varchar,
    website varchar,
    created_date timestamp not null,
    modified_date timestamp not null
);

create table board(
    board_id bigserial primary key,
    name varchar,
    description text,
    closed boolean,
    organization_id bigint references organization(organization_id),
    pinned boolean,
    board_url varchar,
    starred boolean,
    created_date timestamp not null,
    modified_date timestamp not null
);

create table list(
    list_id bigserial primary key,
    name varchar,
    closed boolean,
    board_id bigint references board(board_id),
    pos int,
    created_date timestamp not null,
    modified_date timestamp not null
);

create table card(
    card_id bigserial primary key,
    closed boolean,
    description text,
    due timestamp,
    due_completed boolean,
    board_id bigint references board(board_id),
    list_id bigint,
    name varchar,
    pos int,
    created_date timestamp not null,
    modified_date timestamp not null
);