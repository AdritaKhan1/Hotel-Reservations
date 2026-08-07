package ca.adritakhan.hotel.repository;

import ca.adritakhan.hotel.domain.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByCityIgnoreCaseOrderByName(String city);

    List<Hotel> findAllByOrderByCityAscNameAsc();

    List<String> findDistinctCityByOrderByCity();
}
