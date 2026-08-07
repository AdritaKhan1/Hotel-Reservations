package ca.adritakhan.hotel.repository;

import ca.adritakhan.hotel.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByRoomTypeId(Long roomTypeId);

    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);
}
