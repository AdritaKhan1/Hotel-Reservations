package ca.adritakhan.hotel.service;

import ca.adritakhan.hotel.domain.AppUser;
import ca.adritakhan.hotel.domain.AuthProvider;
import ca.adritakhan.hotel.repository.AppUserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registration and credential checks for local (email + password) accounts. */
@Service
public class AuthService {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(String email, String password, String firstName, String lastName) {
        if (users.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyRegisteredException(
                    "An account already exists for " + email + ". Try logging in instead.");
        }
        AppUser user = AppUser.local(email, passwordEncoder.encode(password), firstName, lastName);
        return users.save(user);
    }

    /** Confirms the email belongs to a password-based account before Spring Security checks the password. */
    public void assertLocalAccount(String email) {
        AppUser user = users.findByEmailIgnoreCase(email).orElse(null);
        if (user != null && user.getProvider() == AuthProvider.GOOGLE && user.getPasswordHash() == null) {
            throw new BadCredentialsException(
                    "This email uses Google sign-in. Use \"Continue with Google\" instead.");
        }
    }
}
