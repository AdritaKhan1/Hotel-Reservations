package ca.adritakhan.hotel.security;

import ca.adritakhan.hotel.domain.AppUser;
import ca.adritakhan.hotel.repository.AppUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Backs local (email + password) login. Google-only accounts still resolve
 * here (so a nice "wrong password" error is possible instead of an NPE) but
 * their {@code passwordHash} never matches anything, since it is never set.
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository users;

    public AppUserDetailsService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("No account for " + email));
        return new AppUserPrincipal(user);
    }
}
