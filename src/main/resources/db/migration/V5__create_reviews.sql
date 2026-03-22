create table reviews (
                         id bigserial primary key,
                         booking_id bigint not null unique,
                         mentor_id bigint not null,
                         student_id bigint not null,
                         rating integer not null,
                         comment varchar(2000),
                         created_at timestamp not null default now(),
                         updated_at timestamp not null default now(),
                         constraint fk_reviews_booking foreign key (booking_id) references bookings(id) on delete cascade,
                         constraint fk_reviews_mentor foreign key (mentor_id) references mentor_profiles(id) on delete cascade,
                         constraint fk_reviews_student foreign key (student_id) references student_profiles(id) on delete cascade,
                         constraint chk_reviews_rating check (rating >= 1 and rating <= 5)
);