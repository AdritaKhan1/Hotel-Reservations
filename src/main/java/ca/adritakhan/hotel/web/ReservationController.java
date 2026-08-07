package ca.adritakhan.hotel.web;

import ca.adritakhan.hotel.domain.AppUser;
import ca.adritakhan.hotel.domain.GuestContact;
import ca.adritakhan.hotel.domain.Reservation;
import ca.adritakhan.hotel.repository.AppUserRepository;
import ca.adritakhan.hotel.repository.ReservationRepository;
import ca.adritakhan.hotel.security.AppUserPrincipal;
import ca.adritakhan.hotel.service.CardValidationService;
import ca.adritakhan.hotel.service.InvalidReservationRequestException;
import ca.adritakhan.hotel.service.MaskedCard;
import ca.adritakhan.hotel.service.ReservationService;
import ca.adritakhan.hotel.web.dto.CreateReservationRequest;
import ca.adritakhan.hotel.web.dto.ReservationResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationRepository reservations;
    private final AppUserRepository appUsers;
    private final CardValidationService cardValidation;

    public ReservationController(ReservationService reservationService, ReservationRepository reservations,
                                 AppUserRepository appUsers, CardValidationService cardValidation) {
        this.reservationService = reservationService;
        this.reservations = reservations;
        this.appUsers = appUsers;
        this.cardValidation = cardValidation;
    }

    /** Requires an authenticated session; see SecurityConfig. The guest is redirected through
     *  login/signup first if they aren't already signed in. */
    @PostMapping("/api/room-types/{roomTypeId}/reservations")
    public ResponseEntity<ReservationResponse> reserve(
            @PathVariable Long roomTypeId,
            @Valid @RequestBody CreateReservationRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal) {

        MaskedCard card = cardValidation.validateAndMask(
                request.cardNumber(), request.cvv(), request.expiryMonth(), request.expiryYear());

        GuestContact contact = buildContact(request, card);

        AppUser user = appUsers.getReferenceById(principal.getUserId());
        Reservation reservation = reservationService.reserve(
                roomTypeId, user, principal.getEmail(), request.checkIn(), request.checkOut(),
                request.rooms(), request.guests(), contact);

        return ResponseEntity
                .created(URI.create("/api/reservations/" + reservation.getId()))
                .body(ReservationResponse.from(reservation));
    }

    @GetMapping("/api/room-types/{roomTypeId}/reservations")
    public List<ReservationResponse> forRoomType(@PathVariable Long roomTypeId) {
        return reservations.findByRoomTypeId(roomTypeId).stream()
                .map(ReservationResponse::from).toList();
    }

    @GetMapping("/api/reservations/me")
    public List<ReservationResponse> mine(@AuthenticationPrincipal AppUserPrincipal principal) {
        return reservations.findByUserIdOrderByCreatedAtDesc(principal.getUserId()).stream()
                .map(ReservationResponse::from).toList();
    }

    private GuestContact buildContact(CreateReservationRequest r, MaskedCard card) {
        boolean sameAddress = r.billingSameAsAddress();
        if (!sameAddress && (isBlank(r.billingAddressLine1()) || isBlank(r.billingCity())
                || isBlank(r.billingRegion()) || isBlank(r.billingPostalCode()) || isBlank(r.billingCountry()))) {
            throw new InvalidReservationRequestException(
                    "Billing address is required when it differs from the guest address");
        }
        return new GuestContact(
                r.firstName(), r.lastName(), r.phone(),
                r.addressLine1(), r.addressLine2(), r.city(), r.region(), r.postalCode(), r.country(),
                sameAddress,
                r.billingAddressLine1(), r.billingAddressLine2(), r.billingCity(),
                r.billingRegion(), r.billingPostalCode(), r.billingCountry(),
                r.cardholderName(), card.brand(), card.last4(), r.expiryMonth(), r.expiryYear());
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
