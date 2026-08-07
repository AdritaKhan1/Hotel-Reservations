package ca.adritakhan.hotel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.LocalDate;

/**
 * How many rooms of a given type are open, and how many are already booked, for
 * ONE night. This is the row that concurrent reservations contend over.
 *
 * The {@link Version} column enables optimistic locking: two reservations that
 * both read "1 room left" on the same night cannot both commit. The second
 * update matches zero rows (version moved) and Hibernate raises a conflict,
 * which the retry layer handles. That is what makes overbooking impossible.
 */
@Entity
@Table(
        name = "room_availability",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_type_id", "stay_date"})
)
public class RoomAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(name = "stay_date", nullable = false)
    private LocalDate stayDate;

    @Column(name = "total_rooms", nullable = false)
    private int totalRooms;

    @Column(name = "booked_rooms", nullable = false)
    private int bookedRooms;

    @Version
    @Column(nullable = false)
    private long version;

    protected RoomAvailability() {
        // required by JPA
    }

    public RoomAvailability(RoomType roomType, LocalDate stayDate, int totalRooms) {
        this.roomType = roomType;
        this.stayDate = stayDate;
        this.totalRooms = totalRooms;
        this.bookedRooms = 0;
    }

    public boolean isSoldOut() {
        return bookedRooms >= totalRooms;
    }

    public int roomsLeft() {
        return totalRooms - bookedRooms;
    }

    public void reserveOne() {
        reserve(1);
    }

    /** Reserves {@code count} rooms on this night, atomically with respect to this row's version. */
    public void reserve(int count) {
        if (roomsLeft() < count) {
            throw new IllegalStateException("Only " + roomsLeft() + " room(s) left on " + stayDate);
        }
        this.bookedRooms += count;
    }

    public void setTotalRooms(int totalRooms) {
        if (totalRooms < bookedRooms) {
            throw new IllegalArgumentException(
                    "Cannot set total below already-booked rooms on " + stayDate);
        }
        this.totalRooms = totalRooms;
    }

    public Long getId() {
        return id;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public LocalDate getStayDate() {
        return stayDate;
    }

    public int getTotalRooms() {
        return totalRooms;
    }

    public int getBookedRooms() {
        return bookedRooms;
    }

    public long getVersion() {
        return version;
    }
}
