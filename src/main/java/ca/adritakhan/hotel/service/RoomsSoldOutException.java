package ca.adritakhan.hotel.service;

import java.time.LocalDate;

/**
 * Thrown when at least one night in the requested stay has no room left (or was
 * never opened for sale). A normal business outcome (HTTP 409), never retried.
 */
public class RoomsSoldOutException extends RuntimeException {
    public RoomsSoldOutException(Long roomTypeId, LocalDate night) {
        super("Room type " + roomTypeId + " is sold out on " + night);
    }
}
