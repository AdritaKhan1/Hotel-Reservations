package ca.adritakhan.hotel.service;

public class RoomTypeNotFoundException extends RuntimeException {
    public RoomTypeNotFoundException(Long roomTypeId) {
        super("No room type found with id " + roomTypeId);
    }
}
