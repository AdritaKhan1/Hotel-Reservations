CREATE TABLE room_types (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL
);

CREATE TABLE room_availability (
    id           BIGSERIAL PRIMARY KEY,
    room_type_id BIGINT  NOT NULL REFERENCES room_types(id),
    stay_date    DATE    NOT NULL,
    total_rooms  INTEGER NOT NULL,
    booked_rooms INTEGER NOT NULL DEFAULT 0,
    version      BIGINT  NOT NULL DEFAULT 0,
    CONSTRAINT uq_type_date UNIQUE (room_type_id, stay_date),
    CONSTRAINT total_not_negative  CHECK (total_rooms >= 0),
    CONSTRAINT booked_not_negative CHECK (booked_rooms >= 0),
    CONSTRAINT never_overbooked    CHECK (booked_rooms <= total_rooms)
);

CREATE TABLE reservations (
    id           BIGSERIAL PRIMARY KEY,
    room_type_id BIGINT       NOT NULL REFERENCES room_types(id),
    guest_name   VARCHAR(255) NOT NULL,
    guest_email  VARCHAR(255) NOT NULL,
    check_in     DATE         NOT NULL,
    check_out    DATE         NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    CONSTRAINT stay_positive CHECK (check_out > check_in)
);

CREATE INDEX idx_reservations_room_type ON reservations(room_type_id);
