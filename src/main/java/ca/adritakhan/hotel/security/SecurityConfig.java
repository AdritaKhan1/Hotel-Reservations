package ca.adritakhan.hotel.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Almost everything is public. This is a browsing/booking site, not a walled
 * garden. The one thing that requires a session is actually placing a
 * reservation. Local login is a custom JSON endpoint (AuthController), not
 * Spring's formLogin.
 *
 * "Continue with Google" is intentionally a demo dead-end (see
 * static/google-demo.html), not a real OAuth2 login. This app used to wire
 * up real Google sign-in, but for a public demo that's unnecessary
 * complexity and an unnecessary real external dependency. If real Google
 * sign-in is wanted again later, that's a `spring-boot-starter-oauth2-client`
 * dependency plus an `.oauth2Login(...)` block here away.
 *
 * CSRF is disabled: this is a same-origin JSON API with no third-party
 * embedding and no cookie-based admin surface, so the usual cross-site
 * form-submission risk CSRF protects against doesn't apply here. A production
 * deployment handling real payments would enable it.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /** Used by local login/register (AuthController) to persist the authenticated session. */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/room-types/*/reservations").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                        (request, response, authException) ->
                                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Login required to reserve"),
                        new AntPathRequestMatcher("/api/**")));
        return http.build();
    }
}
