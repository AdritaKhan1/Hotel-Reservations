package ca.adritakhan.hotel.web.dto;

/** One of the fixed set of destinations shown in the search form. */
public record LocationResponse(String city, String region, String heroImageUrl) {
}
