package ca.adritakhan.hotel.service;

/** What survives checkout once a card has been validated: everything except the PAN and CVV. */
public record MaskedCard(String brand, String last4) {
}
