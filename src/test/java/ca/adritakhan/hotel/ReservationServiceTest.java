package ca.adritakhan.hotel;

import ca.adritakhan.hotel.domain.AppUser;
import ca.adritakhan.hotel.domain.RoomAvailability;
import ca.adritakhan.hotel.domain.RoomType;
import ca.adritakhan.hotel.repository.AppUserRepository;
import ca.adritakhan.hotel.repository.RoomAvailabilityRepository;
import ca.adritakhan.hotel.repository.ReservationRepository;
import ca.adritakhan.hotel.service.InventoryService;
import ca.adritakhan.hotel.service.InvalidStayException;
import ca.adritakhan.hotel.service.ReservationService;
import ca.adritakhan.hotel.service.RoomTypeNotFoundException;
import ca.adritakhan.hotel.service.RoomsSoldOutException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static ca.adritakhan.hotel.TestFixtures.contact;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Non-concurrent behaviour: happy path, multi-night atomic rollback,
 * sold-out night, bad dates, unknown room type.
 */
@SpringBootTest
@Testcontainers
class ReservationServiceTest {

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

    private AppUser guest(String email) {
        return appUsers.save(AppUser.local(email, "not-a-real-hash", "Test", "Guest"));
    }

    @Test
    void reservesEveryNightOfTheStayOnHappyPath() {
        RoomType type = inventory.createRoomType("Deluxe", "Sea view");
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = checkIn.plusDays(3); // 3 nights
        inventory.openInventory(type.getId(), checkIn, checkOut, 2);

        reservationService.reserve(type.getId(), guest("ada@example.com"), "ada@example.com",
                checkIn, checkOut, 1, 2, contact());

        for (LocalDate night : checkIn.datesUntil(checkOut).toList()) {
            RoomAvailability row = availabilityRepository
                    .findByRoomTypeIdAndStayDate(type.getId(), night).orElseThrow();
            assertThat(row.getBookedRooms()).as("night " + night).isEqualTo(1);
        }
        assertThat(reservationRepository.findByRoomTypeId(type.getId())).hasSize(1);
    }

    @Test
    void rollsBackEntireStayIfOneNightIsSoldOut() {
        RoomType type = inventory.createRoomType("Standard", "Two doubles");
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate middle = checkIn.plusDays(1);
        LocalDate checkOut = checkIn.plusDays(3); // nights: checkIn, middle, checkIn+2

        // Open all three nights with 1 room, then sell out only the middle night.
        inventory.openInventory(type.getId(), checkIn, checkOut, 1);
        reservationService.reserve(type.getId(), guest("first@example.com"), "first@example.com",
                middle, middle.plusDays(1), 1, 2, contact());

        // A 3-night stay must fail because the middle night is full...
        assertThatThrownBy(() ->
                reservationService.reserve(type.getId(), guest("second@example.com"), "second@example.com",
                        checkIn, checkOut, 1, 2, contact()))
                .isInstanceOf(RoomsSoldOutException.class);

        // ...and the other two nights must be untouched (atomic rollback).
        RoomAvailability first = availabilityRepository
                .findByRoomTypeIdAndStayDate(type.getId(), checkIn).orElseThrow();
        RoomAvailability last = availabilityRepository
                .findByRoomTypeIdAndStayDate(type.getId(), checkIn.plusDays(2)).orElseThrow();
        assertThat(first.getBookedRooms()).as("first night not charged").isEqualTo(0);
        assertThat(last.getBookedRooms()).as("last night not charged").isEqualTo(0);
        // Only the original single-night reservation exists.
        assertThat(reservationRepository.findByRoomTypeId(type.getId())).hasSize(1);
    }

    @Test
    void rejectsCheckoutOnOrBeforeCheckin() {
        RoomType type = inventory.createRoomType("Any", "Any");
        LocalDate day = LocalDate.now().plusDays(5);
        assertThatThrownBy(() ->
                reservationService.reserve(type.getId(), guest("x@example.com"), "x@example.com",
                        day, day, 1, 2, contact()))
                .isInstanceOf(InvalidStayException.class);
    }

    @Test
    void rejectsTooManyGuestsForRoomsRequested() {
        RoomType type = inventory.createRoomType("Any", "Any"); // maxOccupancy defaults to 2
        LocalDate checkIn = LocalDate.now().plusDays(5);
        inventory.openInventory(type.getId(), checkIn, checkIn.plusDays(1), 1);
        assertThatThrownBy(() ->
                reservationService.reserve(type.getId(), guest("y@example.com"), "y@example.com",
                        checkIn, checkIn.plusDays(1), 1, 5, contact()))
                .isInstanceOf(InvalidStayException.class);
    }

    @Test
    void rejectsUnknownRoomType() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        assertThatThrownBy(() ->
                reservationService.reserve(999_999L, guest("ghost@example.com"), "ghost@example.com",
                        checkIn, checkIn.plusDays(1), 1, 2, contact()))
                .isInstanceOf(RoomTypeNotFoundException.class);
    }
}
