package ca.adritakhan.hotel.service;

/** Thrown for a nonsensical stay, e.g. check-out on or before check-in (HTTP 400). */
public class InvalidStayException extends RuntimeException {
    public InvalidStayException(String message) {
        super(message);
    }
}
