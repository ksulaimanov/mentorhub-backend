create table users (
                       id bigserial primary key,
                       email varchar(255) not null unique,
                       password_hash varchar(255) not null,
                       status varchar(50) not null,
                       email_verified boolean not null default false,
                       created_at timestamp not null default now(),
                       updated_at timestamp not null default now()
);

create table roles (
                       id bigserial primary key,
                       code varchar(50) not null unique
);

create table user_roles (
                            user_id bigint not null,
                            role_id bigint not null,
                            primary key (user_id, role_id),
                            constraint fk_user_roles_user foreign key (user_id) references users(id) on delete cascade,
                            constraint fk_user_roles_role foreign key (role_id) references roles(id) on delete cascade
);

create table refresh_tokens (
                                id bigserial primary key,
                                user_id bigint not null,
                                token varchar(512) not null unique,
                                expires_at timestamp not null,
                                revoked boolean not null default false,
                                created_at timestamp not null default now(),
                                constraint fk_refresh_tokens_user foreign key (user_id) references users(id) on delete cascade
);

insert into roles (code) values ('ROLE_STUDENT');
insert into roles (code) values ('ROLE_MENTOR');
insert into roles (code) values ('ROLE_ADMIN');