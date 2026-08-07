# BookCanada

A concurrent-safe hotel reservation site. Guests search six Canadian
destinations, browse real hotel listings with photos and room details, sign
in, and reserve a stay. The system **never overbooks**, not even when many
guests grab the last room on the same night at the same instant.

Built with Java 21, Spring Boot 3, Spring Security, Spring Data JPA,
PostgreSQL, and Flyway. Tested with JUnit 5 and Testcontainers. Containerized
with Docker and built on GitHub Actions.

## What's here

- **Search** by destination, dates, rooms and guests. No name required up
  front (`static/index.html`).
- **Six destinations**: Toronto, Niagara Falls, Montreal, Banff, Vancouver,
  Blue Mountain. Each has a couple of hotels (Fairmont, Sheraton, Four
  Seasons, Marriott, Westin, and a couple of independents), photos, amenities,
  and several room types (king/queen/double beds, 1- or 2-bedroom, city/falls/
  mountain/ocean views).
- **Hotel detail page** opens in a new tab (`static/hotel.html`) so a guest
  can read everything about a hotel before reserving.
- **Auth-gated checkout**: clicking Reserve prompts login/signup (email +
  password) if the guest isn't already signed in, then collects first/last
  name, phone, address, billing address, and card details, all validated
  (phone format, postal code, well-formed card number, non-expired card)
  before the reservation is placed. "Continue with Google" is present in the
  UI but is a deliberate demo dead-end (see below), not a real login.
- **The concurrency guarantee below is unchanged.** It's still the point of
  the project.

## The problem this solves

The hard case in booking is contention. Two guests read the same night, both
see "1 room left", both write a reservation, and the hotel is now oversold.
Naive code has this bug and it only appears under load.

Inventory is modelled the way hotels actually run it: **per-night allotment**.
A room type has a number of rooms open on each date (`room_availability`), and
a stay reserves N rooms (however many the guest asked for) on every night in
`[check_in, check_out)`.

Overbooking is prevented with **JPA optimistic locking**:

- Each nightly row carries a `@Version` column.
- Every update runs as `UPDATE ... SET booked_rooms = ?, version = version + 1 WHERE id = ? AND version = ?`.
- If two reservations read the same night at version `N` and both try to write,
  only the first commit succeeds. The second matches zero rows and Hibernate
  raises an `OptimisticLockException`, which the retry layer handles.

Two things make this more than a single counter:

- **A stay spans multiple nights, atomically.** All nights are reserved in one
  transaction. If a guest asks for three nights and the middle night doesn't
  have enough rooms left, the nights already decremented are rolled back too;
  a stay is all-or-nothing.
- **Retry vs. reject are different.** `RoomsSoldOutException` means a night is
  genuinely full: return `409`, never retry. An optimistic-lock failure means
  two writers merely collided: re-read and try again, because there may still
  be room. That distinction is the core of the design.

`ReservationService` (retry loop) and `ReservationTransactionService`
(`@Transactional` unit of work) are separate beans on purpose: each attempt
must commit or roll back on its own before the next begins, so the transaction
boundary has to live in a different bean from the loop.

A `CHECK (booked_rooms <= total_rooms)` constraint in the schema is the last
line of defence: even a logic bug cannot oversell the database.

## Auth

Sessions are cookie-based (`HttpSession`). Local accounts use bcrypt via
Spring Security's `DaoAuthenticationProvider`. Register with an email and
password (`AuthController`), and the same session cookie is used everywhere
else.

"Continue with Google" is intentionally **not** wired up to a real Google
account: clicking it takes you to `static/google-demo.html`, which explains
that this is a demo and points back to email/password sign-up. This keeps the
demo self-contained (no external OAuth credentials to configure, nothing that
can silently break because a third-party client id expired) while still
showing the UI/UX of a "sign in with Google" option.

**A real deployment would wire this up for real.** This codebase has done it
before: `spring-boot-starter-oauth2-client` on the classpath, an
`.oauth2Login(...)` block in `SecurityConfig`'s filter chain pointed at a real
Google OAuth Client ID/Secret, and a success handler that finds-or-creates the
matching `AppUser` on first sign-in. Left out here on purpose, not because
it's hard. A public demo shouldn't depend on a real external OAuth client
staying configured and valid.

## Payment details: what's actually stored

This is a demo checkout, not a PCI-DSS-scoped payment processor. The card
number just needs to be 16 digits (no Luhn checksum; any 16 digits you make
up works, e.g. `4242424242424242`) and the CVV 3-4 digits, plus a non-expired
date; `CardValidationService` checks the shape and then **discards** the full
number and CVV. Only the card brand (guessed from the leading digits) and
last 4 digits are ever persisted on a reservation. Nothing beyond that masked
summary is stored or sent anywhere.

**A real payment integration would Luhn-check this field** (and everything
else, up to actually authorizing a charge through a real processor like
Stripe). Skipped here so the checkout form can be exercised with made-up
numbers instead of requiring a specific test card; the Luhn algorithm itself
is straightforward to reintroduce in `CardValidationService.validateAndMask`
if this ever needs to feel stricter.

## Run it

Everything, including the database, comes up with one command:

```bash
docker compose up --build
```

Then open http://localhost:8080. The app seeds ~12 demo hotels across the
six destinations (with ~7 months of nightly inventory) the first time it
starts against an empty database. Set `app.seed-demo-data=false` (or
`-Dapp.seed-demo-data=false`) to start with a blank inventory instead.

Admin-style API access still works directly, e.g. creating a standalone room
type and opening inventory for it:

```bash
curl -X POST http://localhost:8080/api/room-types \
  -H 'Content-Type: application/json' \
  -d '{"name":"Standard King","description":"One king bed"}'

curl -X POST http://localhost:8080/api/room-types/1/availability \
  -H 'Content-Type: application/json' \
  -d '{"from":"2026-09-01","to":"2026-09-05","totalRooms":3}'
```

Reserving requires a session: register, then reserve.

```bash
curl -c cookies.txt -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"ada@example.com","password":"correct-horse","firstName":"Ada","lastName":"Lovelace"}'

curl -b cookies.txt -X POST http://localhost:8080/api/room-types/1/reservations \
  -H 'Content-Type: application/json' \
  -d '{"checkIn":"2026-09-01","checkOut":"2026-09-03","rooms":1,"guests":2,
       "firstName":"Ada","lastName":"Lovelace","phone":"+1 416-555-0100",
       "addressLine1":"123 Main St","city":"Toronto","region":"Ontario","postalCode":"M5V 2T6","country":"Canada",
       "billingSameAsAddress":true,
       "cardholderName":"Ada Lovelace","cardNumber":"4242424242424242","expiryMonth":12,"expiryYear":2030,"cvv":"123"}'
```

## Test it

The tests use Testcontainers, so they need a running Docker daemon. They start
a throwaway PostgreSQL 16 automatically; no local database setup. Demo data
seeding is disabled in tests (`src/test/resources/application.yml`) so it
doesn't slow them down.

```bash
mvn verify
```

- `ReservationConcurrencyIT` fires 60 threads at a 5-room night released
  simultaneously and asserts exactly 5 reservations succeed, every other
  request is cleanly rejected, and the database row counts match.
- `ReservationServiceTest` covers the happy path, the all-or-nothing
  multi-night rollback, sold-out nights, bad dates, too many guests for the
  rooms requested, and unknown room types.

## API

| Method | Path                                          | Purpose                                    |
|--------|-----------------------------------------------|---------------------------------------------|
| GET    | `/api/locations`                              | The six destinations                        |
| GET    | `/api/hotels?city=`                           | Hotels, optionally filtered by city          |
| GET    | `/api/hotels/{id}`                            | One hotel's full listing (photos, room types)|
| POST   | `/api/auth/register`                          | Create a local account (auto logs in)        |
| POST   | `/api/auth/login`                             | Log in with email + password                 |
| POST   | `/api/auth/logout`                            | End the session                              |
| GET    | `/api/auth/me`                                | Current session's user, if any               |
| POST   | `/api/room-types`                             | Create a standalone room type (admin/demo)   |
| GET    | `/api/room-types`                             | List room types                              |
| GET    | `/api/room-types/{id}`                        | Get one room type                            |
| POST   | `/api/room-types/{id}/availability`           | Open nightly allotment (admin)               |
| GET    | `/api/room-types/{id}/availability?from&to`   | Rooms left per night                         |
| POST   | `/api/room-types/{id}/reservations`           | Reserve a stay, requires login               |
| GET    | `/api/room-types/{id}/reservations`           | List reservations for a room type            |
| GET    | `/api/reservations/me`                        | The signed-in guest's own reservations       |

Errors return a JSON body: `409` when a night is sold out or an email is
already registered, `404` for an unknown hotel/room type, `400` for bad
dates/payment/validation, `401` when login is required or credentials are
wrong, `503` if contention exhausts the retry budget.
