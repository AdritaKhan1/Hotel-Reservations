package ca.adritakhan.hotel.service;

/** Thrown when payment details fail format or expiry validation (HTTP 400). */
public class InvalidCardException extends RuntimeException {
    public InvalidCardException(String message) {
        super(message);
    }
}
