create table mentor_availability_slots (
    id bigserial primary key,
    mentor_id bigint not null,
    start_at timestamp not null,
    end_at timestamp not null,
    timezone varchar(100) not null,
    lesson_format varchar(20) not null,
    meeting_link varchar(500),
    address_text varchar(255),
    is_active boolean not null default true,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint fk_availability_slots_mentor foreign key (mentor_id) references mentor_profiles(id) on delete cascade
);