package ca.adritakhan.hotel.web;

import ca.adritakhan.hotel.service.InventoryService;
import ca.adritakhan.hotel.web.dto.AvailabilityResponse;
import ca.adritakhan.hotel.web.dto.OpenInventoryRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
public class AvailabilityController {

    private final InventoryService inventory;

    public AvailabilityController(InventoryService inventory) {
        this.inventory = inventory;
    }

    /** Admin: open N rooms per night for a date range. */
    @PostMapping("/api/room-types/{roomTypeId}/availability")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> open(@PathVariable Long roomTypeId,
                                    @Valid @RequestBody OpenInventoryRequest request) {
        int nights = inventory.openInventory(roomTypeId, request.from(), request.to(), request.totalRooms());
        return Map.of("roomTypeId", roomTypeId, "nightsOpened", nights, "roomsPerNight", request.totalRooms());
    }

    /** Guest: see how many rooms are left each night in a range. */
    @GetMapping("/api/room-types/{roomTypeId}/availability")
    public List<AvailabilityResponse> view(
            @PathVariable Long roomTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return inventory.availabilityFor(roomTypeId, from, to).stream()
                .map(AvailabilityResponse::from).toList();
    }
}
