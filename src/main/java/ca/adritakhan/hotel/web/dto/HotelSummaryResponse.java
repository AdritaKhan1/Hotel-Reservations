package ca.adritakhan.hotel.web.dto;

import ca.adritakhan.hotel.domain.Hotel;

import java.math.BigDecimal;

public record HotelSummaryResponse(
        Long id,
        String name,
        String brand,
        String city,
        String region,
        String heroImageUrl,
        BigDecimal fromNightlyRate
) {
    public static HotelSummaryResponse from(Hotel h, BigDecimal fromNightlyRate) {
        return new HotelSummaryResponse(h.getId(), h.getName(), h.getBrand(), h.getCity(), h.getRegion(),
                h.getHeroImageUrl(), fromNightlyRate);
    }
}
