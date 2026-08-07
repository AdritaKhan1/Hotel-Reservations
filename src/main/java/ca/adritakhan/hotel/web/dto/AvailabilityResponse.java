package ca.adritakhan.hotel.web.dto;

import ca.adritakhan.hotel.domain.RoomAvailability;

import java.time.LocalDate;

public record AvailabilityResponse(
        LocalDate stayDate,
        int totalRooms,
        int bookedRooms,
        int roomsLeft,
        boolean soldOut
) {
    public static AvailabilityResponse from(RoomAvailability a) {
        return new AvailabilityResponse(
                a.getStayDate(), a.getTotalRooms(), a.getBookedRooms(), a.roomsLeft(), a.isSoldOut());
    }
}
