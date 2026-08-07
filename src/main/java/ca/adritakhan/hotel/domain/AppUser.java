package ca.adritakhan.hotel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * A guest account. Local accounts carry a bcrypt {@code passwordHash}; Google
 * accounts carry a {@code googleSub} instead and have no password at all. The
 * two are not mutually exclusive once linked (an email that registered locally
 * can later sign in with Google and the accounts merge into one row).
 */
@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(name = "google_sub", unique = true)
    private String googleSub;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AppUser() {
        // required by JPA
    }

    public static AppUser local(String email, String passwordHash, String firstName, String lastName) {
        AppUser user = new AppUser();
        user.email = email.toLowerCase();
        user.passwordHash = passwordHash;
        user.firstName = firstName;
        user.lastName = lastName;
        user.provider = AuthProvider.LOCAL;
        user.createdAt = Instant.now();
        return user;
    }

    public static AppUser google(String email, String googleSub, String firstName, String lastName) {
        AppUser user = new AppUser();
        user.email = email.toLowerCase();
        user.googleSub = googleSub;
        user.firstName = firstName;
        user.lastName = lastName;
        user.provider = AuthProvider.GOOGLE;
        user.createdAt = Instant.now();
        return user;
    }

    /** Links a Google identity to an existing (typically local) account on first Google sign-in. */
    public void linkGoogle(String googleSub) {
        this.googleSub = googleSub;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public String getGoogleSub() {
        return googleSub;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
