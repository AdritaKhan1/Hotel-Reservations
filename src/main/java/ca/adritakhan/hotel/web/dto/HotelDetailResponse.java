package ca.adritakhan.hotel.web.dto;

import ca.adritakhan.hotel.domain.Hotel;

import java.util.List;

public record HotelDetailResponse(
        Long id,
        String name,
        String brand,
        String city,
        String region,
        String country,
        String description,
        String address,
        String heroImageUrl,
        List<String> imageUrls,
        List<String> amenities,
        List<RoomTypeResponse> roomTypes
) {
    public static HotelDetailResponse from(Hotel h, List<RoomTypeResponse> roomTypes) {
        // Copy out of the lazy collections (List.copyOf forces initialization) while
        // the caller's transaction is still open; see HotelService#getDetail. Handing
        // back Hibernate's own PersistentBag would defer initialization to Jackson
        // serialization, which runs after the session has closed.
        return new HotelDetailResponse(h.getId(), h.getName(), h.getBrand(), h.getCity(), h.getRegion(),
                h.getCountry(), h.getDescription(), h.getAddress(), h.getHeroImageUrl(),
                List.copyOf(h.getImageUrls()), List.copyOf(h.getAmenities()), roomTypes);
    }
}
