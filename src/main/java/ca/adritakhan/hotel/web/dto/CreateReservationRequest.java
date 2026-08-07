package ca.adritakhan.hotel.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * Everything the checkout form collects. The card number and CVV are only
 * ever read by {@link ca.adritakhan.hotel.service.CardValidationService}; see
 * that class for why they are validated here but never persisted.
 */
public record CreateReservationRequest(
        @NotNull LocalDate checkIn,
        @NotNull LocalDate checkOut,
        @Min(1) @Max(10) int rooms,
        @Min(1) @Max(40) int guests,

        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Pattern(regexp = "^[+]?[0-9()\\-\\s]{7,20}$", message = "must be a valid phone number")
        String phone,

        @NotBlank String addressLine1,
        String addressLine2,
        @NotBlank String city,
        @NotBlank String region,
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9\\-\\s]{2,10}$", message = "must be a valid postal/zip code")
        String postalCode,
        @NotBlank String country,

        boolean billingSameAsAddress,
        String billingAddressLine1,
        String billingAddressLine2,
        String billingCity,
        String billingRegion,
        String billingPostalCode,
        String billingCountry,

        @NotBlank String cardholderName,
        // Loose shape check only. CardValidationService enforces the real "16 digits" rule
        // after stripping spaces/dashes, since this pattern has to tolerate formatting like "4242 4242 4242 4242".
        @NotBlank @Pattern(regexp = "^[0-9\\s-]{16,19}$", message = "must be 16 digits")
        String cardNumber,
        @Min(1) @Max(12) int expiryMonth,
        @Min(2000) @Max(2100) int expiryYear,
        @NotBlank @Pattern(regexp = "^[0-9]{3,4}$", message = "must be 3 or 4 digits")
        String cvv
) {
}
