package ca.adritakhan.hotel.service;

/** Thrown at signup when the email is already in use (HTTP 409). */
public class EmailAlreadyRegisteredException extends RuntimeException {
    public EmailAlreadyRegisteredException(String message) {
        super(message);
    }
}
