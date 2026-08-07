package ca.adritakhan.hotel.repository;

import ca.adritakhan.hotel.domain.RoomAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomAvailabilityRepository extends JpaRepository<RoomAvailability, Long> {

    Optional<RoomAvailability> findByRoomTypeIdAndStayDate(Long roomTypeId, LocalDate stayDate);

    List<RoomAvailability> findByRoomTypeIdAndStayDateGreaterThanEqualAndStayDateLessThanOrderByStayDate(
            Long roomTypeId, LocalDate fromInclusive, LocalDate toExclusive);
}
