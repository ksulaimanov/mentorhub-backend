alter table mentor_availability_slots
    add column capacity integer not null default 1;

alter table mentor_availability_slots
    add constraint chk_mentor_availability_slots_capacity
        check (capacity >= 1);

alter table bookings
drop constraint if exists bookings_availability_slot_id_key;