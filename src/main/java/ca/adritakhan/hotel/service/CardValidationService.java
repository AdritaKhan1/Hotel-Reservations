package ca.adritakhan.hotel.service;

import org.springframework.stereotype.Service;

import java.time.YearMonth;

/**
 * Validates payment details at checkout and reduces them to a {@link MaskedCard}.
 * This is deliberately the ONLY place the full card number and CVV are ever
 * looked at: nothing downstream of this class, not the domain, not the
 * database, ever sees or stores the PAN or the CVV. That is a design choice,
 * not an oversight: this is a demo booking flow, not a PCI-DSS-scoped payment
 * processor, so it proves the checkout form is correct without taking on the
 * liability of storing real card data.
 */
@Service
public class CardValidationService {

    /**
     * Validates the card number's length/format, the CVV format, and that the
     * expiry has not passed, then returns a masked, storable summary. Throws
     * {@link InvalidCardException} (HTTP 400) on any failure.
     *
     * Deliberately does NOT run a Luhn checksum: this is a demo with no real
     * payment processor behind it, so any 16-digit number (e.g. one made up
     * for testing) is accepted. There is no real card to actually validate
     * against. A real payment integration would Luhn-check this field; see
     * README's "Payment details" section for that trade-off spelled out.
     */
    public MaskedCard validateAndMask(String cardNumber, String cvv, int expiryMonth, int expiryYear) {
        String digitsOnly = cardNumber == null ? "" : cardNumber.replaceAll("[\\s-]", "");

        if (!digitsOnly.matches("\\d{16}")) {
            throw new InvalidCardException("Card number must be 16 digits");
        }
        if (cvv == null || !cvv.matches("\\d{3,4}")) {
            throw new InvalidCardException("CVV must be 3 or 4 digits");
        }
        if (expiryMonth < 1 || expiryMonth > 12) {
            throw new InvalidCardException("Expiry month must be between 1 and 12");
        }

        YearMonth expiry = YearMonth.of(expiryYear, expiryMonth);
        if (expiry.isBefore(YearMonth.now())) {
            throw new InvalidCardException("Card has expired");
        }

        String brand = detectBrand(digitsOnly);
        String last4 = digitsOnly.substring(digitsOnly.length() - 4);
        return new MaskedCard(brand, last4);
    }

    private String detectBrand(String digits) {
        if (digits.startsWith("4")) {
            return "Visa";
        }
        if (digits.matches("^5[1-5].*") || digits.matches("^2(2[2-9]|[3-6]\\d|7[01])\\d*")) {
            return "Mastercard";
        }
        if (digits.matches("^3[47].*")) {
            return "American Express";
        }
        if (digits.matches("^6(011|5).*")) {
            return "Discover";
        }
        return "Card";
    }
}
