create table  if not exists app_user (
    app_user_id bigserial primary key,
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
	app_user_id bigint not null references app_user on delete cascade,
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

create table app_user_board(
   app_user_id bigint not null,
   board_id bigint not null,
   primary key (app_user_id, board_id),
   foreign key (app_user_id) references app_user(app_user_id) on delete cascade on update cascade,
   foreign key (board_id) references board(board_id) on delete cascade on update cascade
);

insert into app_user (
    app_user_id ,
    avatar_url ,
    avatar_source ,
    bio ,
    confirmed ,
    email ,
    first_name ,
    last_name ,
    user_type ,
    profile_url ,
    password ,
    birth_date ,
    user_name ,
    created_date ,
    modified_date
) values (1, 'url', 'source', 'bio', true, 'email','first_name', 'last_name', 'User', 'profile_url','password', now(),'admin',now(), now());
