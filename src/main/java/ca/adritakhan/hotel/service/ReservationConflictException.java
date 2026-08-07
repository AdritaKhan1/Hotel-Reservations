package ca.adritakhan.hotel.service;

/**
 * Thrown when a reservation could not complete after exhausting retries under
 * heavy contention. Surfaced as HTTP 503 so the client can retry.
 */
public class ReservationConflictException extends RuntimeException {
    public ReservationConflictException(String message) {
        super(message);
    }
}
