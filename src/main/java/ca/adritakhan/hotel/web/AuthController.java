package ca.adritakhan.hotel.web;

import ca.adritakhan.hotel.domain.AppUser;
import ca.adritakhan.hotel.security.AppUserPrincipal;
import ca.adritakhan.hotel.service.AuthService;
import ca.adritakhan.hotel.web.dto.AuthUserResponse;
import ca.adritakhan.hotel.web.dto.LoginRequest;
import ca.adritakhan.hotel.web.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Email/password registration and login, plus {@code /me} so the frontend can
 * check session state on page load. "Continue with Google" is a demo
 * dead-end (see static/google-demo.html) rather than a real login method, so
 * there's no OAuth2 redirect bookkeeping here.
 */
@RestController
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(AuthService authService, AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/api/auth/register")
    public AuthUserResponse register(@Valid @RequestBody RegisterRequest request,
                                     HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        AppUser user = authService.register(request.email(), request.password(), request.firstName(), request.lastName());
        return AuthUserResponse.of(login(user.getEmail(), request.password(), httpRequest, httpResponse));
    }

    @PostMapping("/api/auth/login")
    public AuthUserResponse login(@Valid @RequestBody LoginRequest request,
                                  HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        authService.assertLocalAccount(request.email());
        return AuthUserResponse.of(login(request.email(), request.password(), httpRequest, httpResponse));
    }

    @PostMapping("/api/auth/logout")
    public AuthUserResponse logout(HttpServletRequest request) {
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();
        return AuthUserResponse.anonymous();
    }

    @GetMapping("/api/auth/me")
    public AuthUserResponse me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            return AuthUserResponse.anonymous();
        }
        return AuthUserResponse.of(principal);
    }

    private AppUserPrincipal login(String email, String password,
                                   HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authRequest = new UsernamePasswordAuthenticationToken(email, password);
        Authentication authResult = authenticationManager.authenticate(authRequest);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return (AppUserPrincipal) authResult.getPrincipal();
    }
}
