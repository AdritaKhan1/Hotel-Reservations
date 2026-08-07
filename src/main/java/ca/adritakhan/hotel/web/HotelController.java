package ca.adritakhan.hotel.web;

import ca.adritakhan.hotel.data.Destinations;
import ca.adritakhan.hotel.service.HotelService;
import ca.adritakhan.hotel.web.dto.HotelDetailResponse;
import ca.adritakhan.hotel.web.dto.HotelSummaryResponse;
import ca.adritakhan.hotel.web.dto.LocationResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Browsing hotels: the six destinations, hotels in a destination, and one hotel's full listing. */
@RestController
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping("/api/locations")
    public List<LocationResponse> locations() {
        return Destinations.ALL.stream()
                .map(d -> new LocationResponse(d.city(), d.region(), d.heroImageUrl()))
                .toList();
    }

    @GetMapping("/api/hotels")
    public List<HotelSummaryResponse> list(@RequestParam(required = false) String city) {
        return hotelService.list(city);
    }

    @GetMapping("/api/hotels/{id}")
    public HotelDetailResponse get(@PathVariable Long id) {
        return hotelService.getDetail(id);
    }
}
