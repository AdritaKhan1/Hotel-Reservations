package ca.adritakhan.hotel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

/**
 * A confirmed stay: {@code rooms} rooms of a given type, held for every night
 * in [checkIn, checkOut). The check-out date itself is not a night stayed.
 */
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // EAGER for the same reason as RoomType.hotel: ReservationResponse always
    // needs the room type's (and, transitively, the hotel's) name, and
    // open-in-view is disabled, so a lazy proxy would outlive its session
    // whenever a Reservation is loaded fresh from the repository (as opposed to
    // the in-memory instance built during checkout, which is already initialized).
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "guest_email", nullable = false)
    private String guestEmail;

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    @Column(nullable = false)
    private int rooms;

    @Column(nullable = false)
    private int guests;

    @Embedded
    private GuestContact contact;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Reservation() {
        // required by JPA
    }

    public Reservation(RoomType roomType, AppUser user, String guestEmail, LocalDate checkIn, LocalDate checkOut,
                       int rooms, int guests, GuestContact contact) {
        this.roomType = roomType;
        this.user = user;
        this.guestEmail = guestEmail;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.rooms = rooms;
        this.guests = guests;
        this.contact = contact;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public AppUser getUser() {
        return user;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public int getRooms() {
        return rooms;
    }

    public int getGuests() {
        return guests;
    }

    public GuestContact getContact() {
        return contact;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
