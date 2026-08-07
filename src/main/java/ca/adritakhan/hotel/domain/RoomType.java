package ca.adritakhan.hotel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * A category of room a hotel sells, e.g. "King Suite, Falls View". Inventory
 * is not held here; it is held per night in {@link RoomAvailability}, which is
 * how real hotel systems model allotment (a room type can have different
 * numbers of rooms open on different dates).
 */
@Entity
@Table(name = "room_types")
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // EAGER (not the LAZY default elsewhere in this codebase) is deliberate: every
    // read path (hotel listings, checkout, reservation confirmation) needs the
    // hotel's name/city alongside the room type, and open-in-view is disabled
    // (see application.yml), so a lazy proxy here would throw
    // LazyInitializationException once the session that loaded it has closed.
    // A many-to-one like this is a single cheap join, not an N+1 risk.
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "bed_type", nullable = false)
    private BedType bedType;

    @Column(nullable = false)
    private int bedrooms;

    @Column(name = "view_type", nullable = false)
    private String viewType;

    @Column(name = "max_occupancy", nullable = false)
    private int maxOccupancy;

    @Column(name = "nightly_rate", nullable = false)
    private BigDecimal nightlyRate;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    protected RoomType() {
        // required by JPA
    }

    public RoomType(Hotel hotel, String name, String description, BedType bedType, int bedrooms,
                    String viewType, int maxOccupancy, BigDecimal nightlyRate, String imageUrl) {
        this.hotel = hotel;
        this.name = name;
        this.description = description;
        this.bedType = bedType;
        this.bedrooms = bedrooms;
        this.viewType = viewType;
        this.maxOccupancy = maxOccupancy;
        this.nightlyRate = nightlyRate;
        this.imageUrl = imageUrl;
    }

    /** Convenience constructor for callers (e.g. admin tooling, tests) that don't need the full retail listing. */
    public RoomType(String name, String description) {
        this(null, name, description, BedType.QUEEN, 1, "Standard View", 2, new BigDecimal("199.00"), "");
    }

    public Long getId() {
        return id;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BedType getBedType() {
        return bedType;
    }

    public int getBedrooms() {
        return bedrooms;
    }

    public String getViewType() {
        return viewType;
    }

    public int getMaxOccupancy() {
        return maxOccupancy;
    }

    public BigDecimal getNightlyRate() {
        return nightlyRate;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
