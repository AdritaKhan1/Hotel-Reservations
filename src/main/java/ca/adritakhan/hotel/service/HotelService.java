package ca.adritakhan.hotel.service;

import ca.adritakhan.hotel.domain.Hotel;
import ca.adritakhan.hotel.domain.RoomType;
import ca.adritakhan.hotel.repository.HotelRepository;
import ca.adritakhan.hotel.repository.RoomTypeRepository;
import ca.adritakhan.hotel.web.dto.HotelDetailResponse;
import ca.adritakhan.hotel.web.dto.HotelSummaryResponse;
import ca.adritakhan.hotel.web.dto.RoomTypeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Read-side hotel browsing. {@link #getDetail} is deliberately {@code
 * @Transactional}: {@link Hotel#getImageUrls()} and {@link Hotel#getAmenities()}
 * are lazy element collections, and with {@code open-in-view: false} the
 * session that loaded the hotel closes the instant the repository call
 * returns. So building the response has to happen while that session is
 * still open, not back in the controller.
 */
@Service
public class HotelService {

    private final HotelRepository hotels;
    private final RoomTypeRepository roomTypes;

    public HotelService(HotelRepository hotels, RoomTypeRepository roomTypes) {
        this.hotels = hotels;
        this.roomTypes = roomTypes;
    }

    @Transactional(readOnly = true)
    public List<HotelSummaryResponse> list(String city) {
        List<Hotel> found = (city == null || city.isBlank())
                ? hotels.findAllByOrderByCityAscNameAsc()
                : hotels.findByCityIgnoreCaseOrderByName(city);

        return found.stream()
                .map(h -> HotelSummaryResponse.from(h, cheapestRate(h.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public HotelDetailResponse getDetail(Long hotelId) {
        Hotel hotel = hotels.findById(hotelId).orElseThrow(() -> new HotelNotFoundException(hotelId));
        List<RoomTypeResponse> types = roomTypes.findByHotelIdOrderByNightlyRate(hotelId).stream()
                .map(RoomTypeResponse::from)
                .toList();
        return HotelDetailResponse.from(hotel, types);
    }

    private BigDecimal cheapestRate(Long hotelId) {
        return roomTypes.findByHotelIdOrderByNightlyRate(hotelId).stream()
                .map(RoomType::getNightlyRate)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
    }
}
