package ca.adritakhan.hotel.web.dto;

import ca.adritakhan.hotel.security.AppUserPrincipal;

public record AuthUserResponse(boolean authenticated, Long id, String email, String firstName, String lastName) {

    public static AuthUserResponse of(AppUserPrincipal principal) {
        return new AuthUserResponse(true, principal.getUserId(), principal.getEmail(),
                principal.getFirstName(), principal.getLastName());
    }

    public static AuthUserResponse anonymous() {
        return new AuthUserResponse(false, null, null, null, null);
    }
}
