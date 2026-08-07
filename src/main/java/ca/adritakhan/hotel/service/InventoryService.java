package ca.adritakhan.hotel.service;

import ca.adritakhan.hotel.domain.RoomAvailability;
import ca.adritakhan.hotel.domain.RoomType;
import ca.adritakhan.hotel.repository.RoomAvailabilityRepository;
import ca.adritakhan.hotel.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Admin-side operations: create room types and open nightly allotment for a
 * date range. Opening inventory up front means reservations only ever UPDATE
 * existing nightly rows, so guests never race to INSERT the same row.
 */
@Service
public class InventoryService {

    private final RoomTypeRepository roomTypes;
    private final RoomAvailabilityRepository availability;

    public InventoryService(RoomTypeRepository roomTypes, RoomAvailabilityRepository availability) {
        this.roomTypes = roomTypes;
        this.availability = availability;
    }

    @Transactional
    public RoomType createRoomType(String name, String description) {
        return roomTypes.save(new RoomType(name, description));
    }

    /**
     * Open (or adjust) {@code totalRooms} rooms for every night in
     * [fromInclusive, toExclusive). Idempotent: re-opening a night updates its
     * total rather than duplicating it.
     */
    @Transactional
    public int openInventory(Long roomTypeId, LocalDate fromInclusive, LocalDate toExclusive, int totalRooms) {
        if (!toExclusive.isAfter(fromInclusive)) {
            throw new InvalidStayException("End date must be after start date");
        }
        RoomType type = roomTypes.findById(roomTypeId)
                .orElseThrow(() -> new RoomTypeNotFoundException(roomTypeId));

        int nights = 0;
        for (LocalDate night : fromInclusive.datesUntil(toExclusive).toList()) {
            RoomAvailability row = availability
                    .findByRoomTypeIdAndStayDate(roomTypeId, night)
                    .orElseGet(() -> new RoomAvailability(type, night, totalRooms));
            row.setTotalRooms(totalRooms);
            availability.save(row);
            nights++;
        }
        return nights;
    }

    @Transactional(readOnly = true)
    public List<RoomAvailability> availabilityFor(Long roomTypeId, LocalDate fromInclusive, LocalDate toExclusive) {
        return availability
                .findByRoomTypeIdAndStayDateGreaterThanEqualAndStayDateLessThanOrderByStayDate(
                        roomTypeId, fromInclusive, toExclusive);
    }
}
