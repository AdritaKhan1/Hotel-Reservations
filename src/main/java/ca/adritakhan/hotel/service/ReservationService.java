package ca.adritakhan.hotel.service;

import ca.adritakhan.hotel.domain.AppUser;
import ca.adritakhan.hotel.domain.GuestContact;
import ca.adritakhan.hotel.domain.Reservation;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Public entry point for making a reservation. Wraps the transactional attempt
 * in a bounded retry loop.
 *
 *   - RoomsSoldOutException           -> a night is genuinely full. Propagate (409).
 *   - OptimisticLockingFailureException -> two guests collided on a night's version.
 *                                          Re-read and try again; there may still be
 *                                          room after the other transaction settled.
 *
 * Only the second is retried.
 */
@Service
public class ReservationService {

    private static final int MAX_ATTEMPTS = 8;

    private final ReservationTransactionService tx;

    public ReservationService(ReservationTransactionService tx) {
        this.tx = tx;
    }

    public Reservation reserve(Long roomTypeId, AppUser user, String guestEmail,
                               LocalDate checkIn, LocalDate checkOut,
                               int rooms, int guests, GuestContact contact) {
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                return tx.attemptReservation(roomTypeId, user, guestEmail, checkIn, checkOut, rooms, guests, contact);
            } catch (OptimisticLockingFailureException conflict) {
                if (attempt >= MAX_ATTEMPTS) {
                    throw new ReservationConflictException(
                            "Could not reserve room type " + roomTypeId + " after " + MAX_ATTEMPTS
                                    + " attempts due to concurrent demand. Please retry.");
                }
                backoff(attempt);
            }
        }
    }

    private void backoff(int attempt) {
        try {
            long millis = (long) (Math.random() * attempt * 5);
            if (millis > 0) {
                Thread.sleep(millis);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ReservationConflictException("Interrupted while reserving");
        }
    }
}
