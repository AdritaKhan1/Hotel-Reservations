package ca.adritakhan.hotel.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * A property in a city that sells {@link RoomType}s. Kept deliberately simple:
 * a name, a brand, a location, some pictures and amenities. The interesting
 * invariants in this codebase live in inventory and reservations, not in
 * property data.
 */
@Entity
@Table(name = "hotels")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** e.g. "Fairmont", "Sheraton", "Four Seasons"; null/blank for independent properties. */
    @Column
    private String brand;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private String address;

    @Column(name = "hero_image_url", nullable = false, length = 500)
    private String heroImageUrl;

    @ElementCollection
    @CollectionTable(name = "hotel_images", joinColumns = @JoinColumn(name = "hotel_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "image_url", length = 500)
    private List<String> imageUrls = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "hotel_amenities", joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "amenity")
    private List<String> amenities = new ArrayList<>();

    protected Hotel() {
        // required by JPA
    }

    public Hotel(String name, String brand, String city, String region, String country,
                 String description, String address, String heroImageUrl,
                 List<String> imageUrls, List<String> amenities) {
        this.name = name;
        this.brand = brand;
        this.city = city;
        this.region = region;
        this.country = country;
        this.description = description;
        this.address = address;
        this.heroImageUrl = heroImageUrl;
        this.imageUrls = new ArrayList<>(imageUrls);
        this.amenities = new ArrayList<>(amenities);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public String getCity() {
        return city;
    }

    public String getRegion() {
        return region;
    }

    public String getCountry() {
        return country;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public String getHeroImageUrl() {
        return heroImageUrl;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public List<String> getAmenities() {
        return amenities;
    }
}
