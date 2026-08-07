package ca.adritakhan.hotel;

import ca.adritakhan.hotel.domain.GuestContact;

/** Shared checkout-details fixture for tests that don't care about the specific values. */
final class TestFixtures {

    private TestFixtures() {
    }

    static GuestContact contact() {
        return new GuestContact(
                "Ada", "Lovelace", "+1 416-555-0100",
                "123 Main St", null, "Toronto", "Ontario", "M5V 2T6", "Canada",
                true, null, null, null, null, null, null,
                "Ada Lovelace", "Visa", "4242", 12, 2030);
    }
}
