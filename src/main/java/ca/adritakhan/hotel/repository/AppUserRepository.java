package ca.adritakhan.hotel.repository;

import ca.adritakhan.hotel.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmailIgnoreCase(String email);

    Optional<AppUser> findByGoogleSub(String googleSub);

    boolean existsByEmailIgnoreCase(String email);
}
