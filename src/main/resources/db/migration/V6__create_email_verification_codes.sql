create table email_verification_codes (
                                          id bigserial primary key,
                                          user_id bigint not null,
                                          code varchar(10) not null,
                                          expires_at timestamp not null,
                                          used boolean not null default false,
                                          attempts integer not null default 0,
                                          created_at timestamp not null default now(),
                                          constraint fk_email_verification_codes_user
                                              foreign key (user_id) references users(id) on delete cascade
);

create index idx_email_verification_codes_user_id
    on email_verification_codes(user_id);

create index idx_email_verification_codes_code
    on email_verification_codes(code);