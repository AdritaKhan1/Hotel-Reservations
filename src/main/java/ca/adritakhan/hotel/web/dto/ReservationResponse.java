package ca.adritakhan.hotel.web.dto;

import ca.adritakhan.hotel.domain.Reservation;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record ReservationResponse(
        Long id,
        Long roomTypeId,
        String roomTypeName,
        String hotelName,
        String hotelCity,
        String guestEmail,
        String firstName,
        String lastName,
        LocalDate checkIn,
        LocalDate checkOut,
        int nights,
        int rooms,
        int guests,
        BigDecimal totalPrice,
        String cardBrand,
        String cardLast4,
        Instant createdAt
) {
    public static ReservationResponse from(Reservation r) {
        long nights = ChronoUnit.DAYS.between(r.getCheckIn(), r.getCheckOut());
        BigDecimal total = r.getRoomType().getNightlyRate()
                .multiply(BigDecimal.valueOf(nights))
                .multiply(BigDecimal.valueOf(r.getRooms()));
        return new ReservationResponse(
                r.getId(), r.getRoomType().getId(), r.getRoomType().getName(),
                r.getRoomType().getHotel() == null ? null : r.getRoomType().getHotel().getName(),
                r.getRoomType().getHotel() == null ? null : r.getRoomType().getHotel().getCity(),
                r.getGuestEmail(), r.getContact().getFirstName(), r.getContact().getLastName(),
                r.getCheckIn(), r.getCheckOut(), (int) nights, r.getRooms(), r.getGuests(), total,
                r.getContact().getCardBrand(), r.getContact().getCardLast4(), r.getCreatedAt());
    }
}
