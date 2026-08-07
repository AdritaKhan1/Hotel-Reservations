-- Hotels: the physical property a room type belongs to.
CREATE TABLE hotels (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    brand        VARCHAR(255),
    city         VARCHAR(255) NOT NULL,
    region       VARCHAR(255) NOT NULL,
    country      VARCHAR(255) NOT NULL DEFAULT 'Canada',
    description  VARCHAR(2000) NOT NULL,
    address      VARCHAR(255) NOT NULL,
    hero_image_url VARCHAR(500) NOT NULL
);

CREATE INDEX idx_hotels_city ON hotels(city);

CREATE TABLE hotel_images (
    hotel_id  BIGINT NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_hotel_images_hotel ON hotel_images(hotel_id);

CREATE TABLE hotel_amenities (
    hotel_id  BIGINT NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    amenity   VARCHAR(255) NOT NULL
);

CREATE INDEX idx_hotel_amenities_hotel ON hotel_amenities(hotel_id);

-- Room types now belong to a hotel and describe what a guest actually books.
ALTER TABLE room_types
    ADD COLUMN hotel_id       BIGINT REFERENCES hotels(id),
    ADD COLUMN bed_type       VARCHAR(20)    NOT NULL DEFAULT 'QUEEN',
    ADD COLUMN bedrooms       INTEGER        NOT NULL DEFAULT 1,
    ADD COLUMN view_type      VARCHAR(100)   NOT NULL DEFAULT 'Standard View',
    ADD COLUMN max_occupancy  INTEGER        NOT NULL DEFAULT 2,
    ADD COLUMN nightly_rate   NUMERIC(10,2)  NOT NULL DEFAULT 199.00,
    ADD COLUMN image_url      VARCHAR(500)   NOT NULL DEFAULT '';

CREATE INDEX idx_room_types_hotel ON room_types(hotel_id);

-- Guest accounts. password_hash is NULL for Google-only accounts.
CREATE TABLE app_users (
    id             BIGSERIAL PRIMARY KEY,
    email          VARCHAR(255) NOT NULL UNIQUE,
    password_hash  VARCHAR(255),
    first_name     VARCHAR(255),
    last_name      VARCHAR(255),
    provider       VARCHAR(20)  NOT NULL,
    google_sub     VARCHAR(255) UNIQUE,
    created_at     TIMESTAMP    NOT NULL
);

-- Reservations gain a room count, a guest count, the account that booked them,
-- and the contact / billing / payment details collected at checkout. The card
-- is never stored beyond brand + last 4 + expiry. The PAN and CVV are
-- validated on the way in and discarded.
ALTER TABLE reservations
    ADD COLUMN user_id                  BIGINT REFERENCES app_users(id),
    ADD COLUMN rooms                    INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN guests                   INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN first_name               VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN last_name                VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN phone                    VARCHAR(30)  NOT NULL DEFAULT '',
    ADD COLUMN address_line1            VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN address_line2            VARCHAR(255),
    ADD COLUMN city                     VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN region                   VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN postal_code              VARCHAR(20)  NOT NULL DEFAULT '',
    ADD COLUMN country                  VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN billing_same_as_address  BOOLEAN      NOT NULL DEFAULT TRUE,
    ADD COLUMN billing_address_line1    VARCHAR(255),
    ADD COLUMN billing_address_line2    VARCHAR(255),
    ADD COLUMN billing_city             VARCHAR(255),
    ADD COLUMN billing_region           VARCHAR(255),
    ADD COLUMN billing_postal_code      VARCHAR(20),
    ADD COLUMN billing_country          VARCHAR(255),
    ADD COLUMN cardholder_name          VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN card_brand               VARCHAR(20)  NOT NULL DEFAULT '',
    ADD COLUMN card_last4               VARCHAR(4)   NOT NULL DEFAULT '',
    ADD COLUMN card_expiry_month        INTEGER      NOT NULL DEFAULT 1,
    ADD COLUMN card_expiry_year         INTEGER      NOT NULL DEFAULT 2000,
    DROP COLUMN guest_name;

CREATE INDEX idx_reservations_user ON reservations(user_id);
