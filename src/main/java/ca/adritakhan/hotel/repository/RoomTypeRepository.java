package ca.adritakhan.hotel.repository;

import ca.adritakhan.hotel.domain.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    List<RoomType> findByHotelIdOrderByNightlyRate(Long hotelId);
}
