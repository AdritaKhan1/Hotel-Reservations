package ca.adritakhan.hotel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Everything collected at checkout: who is staying, where to reach and bill
 * them, and the payment method (masked). The full card number and CVV never
 * reach this type; {@code cardBrand}/{@code cardLast4} are all that is kept,
 * because a card is only ever validated at checkout, never stored in full.
 */
@Embeddable
public class GuestContact {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "address_line1", nullable = false)
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "region", nullable = false)
    private String region;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "billing_same_as_address", nullable = false)
    private boolean billingSameAsAddress;

    @Column(name = "billing_address_line1")
    private String billingAddressLine1;

    @Column(name = "billing_address_line2")
    private String billingAddressLine2;

    @Column(name = "billing_city")
    private String billingCity;

    @Column(name = "billing_region")
    private String billingRegion;

    @Column(name = "billing_postal_code")
    private String billingPostalCode;

    @Column(name = "billing_country")
    private String billingCountry;

    @Column(name = "cardholder_name", nullable = false)
    private String cardholderName;

    @Column(name = "card_brand", nullable = false)
    private String cardBrand;

    @Column(name = "card_last4", nullable = false, length = 4)
    private String cardLast4;

    @Column(name = "card_expiry_month", nullable = false)
    private int cardExpiryMonth;

    @Column(name = "card_expiry_year", nullable = false)
    private int cardExpiryYear;

    protected GuestContact() {
        // required by JPA
    }

    public GuestContact(String firstName, String lastName, String phone,
                        String addressLine1, String addressLine2, String city, String region,
                        String postalCode, String country, boolean billingSameAsAddress,
                        String billingAddressLine1, String billingAddressLine2, String billingCity,
                        String billingRegion, String billingPostalCode, String billingCountry,
                        String cardholderName, String cardBrand, String cardLast4,
                        int cardExpiryMonth, int cardExpiryYear) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.region = region;
        this.postalCode = postalCode;
        this.country = country;
        this.billingSameAsAddress = billingSameAsAddress;
        this.billingAddressLine1 = billingSameAsAddress ? addressLine1 : billingAddressLine1;
        this.billingAddressLine2 = billingSameAsAddress ? addressLine2 : billingAddressLine2;
        this.billingCity = billingSameAsAddress ? city : billingCity;
        this.billingRegion = billingSameAsAddress ? region : billingRegion;
        this.billingPostalCode = billingSameAsAddress ? postalCode : billingPostalCode;
        this.billingCountry = billingSameAsAddress ? country : billingCountry;
        this.cardholderName = cardholderName;
        this.cardBrand = cardBrand;
        this.cardLast4 = cardLast4;
        this.cardExpiryMonth = cardExpiryMonth;
        this.cardExpiryYear = cardExpiryYear;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getCity() {
        return city;
    }

    public String getRegion() {
        return region;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    public boolean isBillingSameAsAddress() {
        return billingSameAsAddress;
    }

    public String getBillingAddressLine1() {
        return billingAddressLine1;
    }

    public String getBillingAddressLine2() {
        return billingAddressLine2;
    }

    public String getBillingCity() {
        return billingCity;
    }

    public String getBillingRegion() {
        return billingRegion;
    }

    public String getBillingPostalCode() {
        return billingPostalCode;
    }

    public String getBillingCountry() {
        return billingCountry;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public String getCardLast4() {
        return cardLast4;
    }

    public int getCardExpiryMonth() {
        return cardExpiryMonth;
    }

    public int getCardExpiryYear() {
        return cardExpiryYear;
    }
}
