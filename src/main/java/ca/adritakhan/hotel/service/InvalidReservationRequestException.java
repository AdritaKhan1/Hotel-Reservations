package ca.adritakhan.hotel.service;

/** Thrown for checkout-form problems that span multiple fields (HTTP 400), e.g. a missing billing address. */
public class InvalidReservationRequestException extends RuntimeException {
    public InvalidReservationRequestException(String message) {
        super(message);
    }
}
