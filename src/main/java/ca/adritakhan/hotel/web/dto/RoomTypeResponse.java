package ca.adritakhan.hotel.web.dto;

import ca.adritakhan.hotel.domain.BedType;
import ca.adritakhan.hotel.domain.RoomType;

import java.math.BigDecimal;

public record RoomTypeResponse(
        Long id,
        Long hotelId,
        String hotelName,
        String name,
        String description,
        BedType bedType,
        int bedrooms,
        String viewType,
        int maxOccupancy,
        BigDecimal nightlyRate,
        String imageUrl
) {
    public static RoomTypeResponse from(RoomType t) {
        return new RoomTypeResponse(
                t.getId(),
                t.getHotel() == null ? null : t.getHotel().getId(),
                t.getHotel() == null ? null : t.getHotel().getName(),
                t.getName(), t.getDescription(), t.getBedType(), t.getBedrooms(), t.getViewType(),
                t.getMaxOccupancy(), t.getNightlyRate(), t.getImageUrl());
    }
}
