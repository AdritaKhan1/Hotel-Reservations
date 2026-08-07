package ca.adritakhan.hotel.service;

import ca.adritakhan.hotel.domain.AppUser;
import ca.adritakhan.hotel.domain.GuestContact;
import ca.adritakhan.hotel.domain.RoomAvailability;
import ca.adritakhan.hotel.domain.RoomType;
import ca.adritakhan.hotel.domain.Reservation;
import ca.adritakhan.hotel.repository.RoomAvailabilityRepository;
import ca.adritakhan.hotel.repository.RoomTypeRepository;
import ca.adritakhan.hotel.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * One reservation attempt, as a single transaction.
 *
 * A stay spans several nights, so this reserves {@code rooms} rooms on EVERY
 * night in the range. Each night's row is guarded by its @Version, and every
 * night is reserved in the same transaction. So if the guest asks for three
 * nights and the middle night doesn't have enough rooms left, the nights
 * already decremented are rolled back too. A stay is all-or-nothing.
 *
 * Kept separate from {@link ReservationService} so the retry loop sits outside
 * the transaction and each attempt commits or rolls back cleanly on its own.
 */
@Service
public class ReservationTransactionService {

    private final RoomTypeRepository roomTypes;
    private final RoomAvailabilityRepository availability;
    private final ReservationRepository reservations;

    public ReservationTransactionService(RoomTypeRepository roomTypes,
                                         RoomAvailabilityRepository availability,
                                         ReservationRepository reservations) {
        this.roomTypes = roomTypes;
        this.availability = availability;
        this.reservations = reservations;
    }

    @Transactional
    public Reservation attemptReservation(Long roomTypeId, AppUser user, String guestEmail,
                                          LocalDate checkIn, LocalDate checkOut,
                                          int rooms, int guests, GuestContact contact) {
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidStayException("Check-out (" + checkOut + ") must be after check-in (" + checkIn + ")");
        }
        if (rooms < 1) {
            throw new InvalidStayException("Rooms requested must be at least 1");
        }
        if (guests < 1) {
            throw new InvalidStayException("Guests must be at least 1");
        }

        RoomType type = roomTypes.findById(roomTypeId)
                .orElseThrow(() -> new RoomTypeNotFoundException(roomTypeId));

        if (guests > type.getMaxOccupancy() * rooms) {
            throw new InvalidStayException(
                    "This room type sleeps " + type.getMaxOccupancy() + " per room; "
                            + rooms + " room(s) cannot hold " + guests + " guest(s)");
        }

        for (LocalDate night : checkIn.datesUntil(checkOut).toList()) {
            RoomAvailability row = availability
                    .findByRoomTypeIdAndStayDate(roomTypeId, night)
                    .orElseThrow(() -> new RoomsSoldOutException(roomTypeId, night));

            if (row.roomsLeft() < rooms) {
                throw new RoomsSoldOutException(roomTypeId, night);
            }

            row.reserve(rooms);
            // Flush now so a version conflict on this night surfaces immediately,
            // as an OptimisticLockingFailureException the retry layer can catch.
            availability.saveAndFlush(row);
        }

        Reservation reservation = new Reservation(type, user, guestEmail, checkIn, checkOut, rooms, guests, contact);
        return reservations.save(reservation);
    }
}
