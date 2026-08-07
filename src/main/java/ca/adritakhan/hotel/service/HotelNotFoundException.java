package ca.adritakhan.hotel.service;

public class HotelNotFoundException extends RuntimeException {
    public HotelNotFoundException(Long hotelId) {
        super("No hotel found with id " + hotelId);
    }
}
