package ca.adritakhan.hotel;

import ca.adritakhan.hotel.domain.AppUser;
import ca.adritakhan.hotel.domain.RoomAvailability;
import ca.adritakhan.hotel.domain.RoomType;
import ca.adritakhan.hotel.repository.AppUserRepository;
import ca.adritakhan.hotel.repository.RoomAvailabilityRepository;
import ca.adritakhan.hotel.repository.ReservationRepository;
import ca.adritakhan.hotel.service.InventoryService;
import ca.adritakhan.hotel.service.ReservationConflictException;
import ca.adritakhan.hotel.service.ReservationService;
import ca.adritakhan.hotel.service.RoomsSoldOutException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The point of the whole project.
 *
 * A REAL PostgreSQL (via Testcontainers, not H2) is started so optimistic-lock
 * behaviour is exercised against the production database engine. Then many
 * guests are released at the same instant against a room type with only a few
 * rooms open, and the invariant that must never break is asserted: the number
 * of confirmed reservations equals the rooms available, never more.
 */
@SpringBootTest
@Testcontainers
class ReservationConcurrencyIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private InventoryService inventory;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private RoomAvailabilityRepository availabilityRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private AppUserRepository appUsers;

    @Test
    void neverOverbooksUnderConcurrentDemand() throws InterruptedException {
        int roomsPerNight = 5;
        int contenders = 60;
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = checkIn.plusDays(1); // single night

        RoomType type = inventory.createRoomType("Standard King", "One king bed");
        Long typeId = type.getId();
        inventory.openInventory(typeId, checkIn, checkOut, roomsPerNight);

        // Created up front, sequentially, so account creation isn't part of the race being tested.
        List<AppUser> guests = new ArrayList<>();
        for (int i = 0; i < contenders; i++) {
            guests.add(appUsers.save(AppUser.local("guest" + i + "@example.com", "not-a-real-hash", "Guest", "" + i)));
        }

        ExecutorService pool = Executors.newFixedThreadPool(contenders);
        CountDownLatch everyoneReady = new CountDownLatch(contenders);
        CountDownLatch startGun = new CountDownLatch(1);

        AtomicInteger confirmed = new AtomicInteger();
        AtomicInteger soldOut = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < contenders; i++) {
            final int n = i;
            futures.add(pool.submit(() -> {
                everyoneReady.countDown();
                startGun.await();                       // all threads block here...
                try {
                    AppUser guest = guests.get(n);
                    reservationService.reserve(typeId, guest, guest.getEmail(), checkIn, checkOut,
                            1, 2, TestFixtures.contact());
                    confirmed.incrementAndGet();
                } catch (RoomsSoldOutException e) {
                    soldOut.incrementAndGet();
                } catch (ReservationConflictException e) {
                    conflict.incrementAndGet();
                }
                return null;
            }));
        }

        everyoneReady.await();
        startGun.countDown();                            // ...released simultaneously
        pool.shutdown();
        boolean finished = pool.awaitTermination(60, TimeUnit.SECONDS);
        assertThat(finished).as("all reservation attempts completed").isTrue();

        // The invariant: exactly `roomsPerNight` reservations won, never more.
        assertThat(confirmed.get()).as("confirmed reservations").isEqualTo(roomsPerNight);

        // Every other guest got a definite outcome; none silently lost.
        assertThat(confirmed.get() + soldOut.get() + conflict.get())
                .as("every contender got a definite outcome")
                .isEqualTo(contenders);

        // The database agrees with the application.
        RoomAvailability night = availabilityRepository
                .findByRoomTypeIdAndStayDate(typeId, checkIn).orElseThrow();
        assertThat(night.getBookedRooms()).as("booked_rooms in DB").isEqualTo(roomsPerNight);
        assertThat(reservationRepository.findByRoomTypeId(typeId)).as("reservation rows").hasSize(roomsPerNight);
    }
}
