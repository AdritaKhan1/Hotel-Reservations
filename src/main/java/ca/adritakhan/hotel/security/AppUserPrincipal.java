package ca.adritakhan.hotel.security;

import ca.adritakhan.hotel.domain.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/**
 * The one {@link UserDetails} type used for every authenticated request
 * (local email/password login is the only real login method; see
 * {@link ca.adritakhan.hotel.security.SecurityConfig} for why "Continue with
 * Google" is a demo dead-end rather than a second login method here).
 */
public class AppUserPrincipal implements UserDetails {

    private final Long userId;
    private final String email;
    private final String passwordHash;
    private final String firstName;
    private final String lastName;

    public AppUserPrincipal(AppUser user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
