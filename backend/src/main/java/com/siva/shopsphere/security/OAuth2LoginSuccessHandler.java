package com.siva.shopsphere.security;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.siva.shopsphere.accounts.entity.AuthProvider;
import com.siva.shopsphere.accounts.entity.User;
import com.siva.shopsphere.accounts.entity.UserRole;
import com.siva.shopsphere.accounts.repository.UserRepository;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    private final UserRepository userRepository;
    private final OAuth2CodeService oauth2CodeService;
    private final PasswordEncoder passwordEncoder;
    private final String frontendUrl;

    public OAuth2LoginSuccessHandler(
            UserRepository userRepository,
            OAuth2CodeService oauth2CodeService,
            PasswordEncoder passwordEncoder,
            @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.userRepository = userRepository;
        this.oauth2CodeService = oauth2CodeService;
        this.passwordEncoder = passwordEncoder;
        this.frontendUrl = frontendUrl;
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        
        String email = oauth2User.getAttribute("email");
        String subject = oauth2User.getAttribute("sub");
        String givenName = oauth2User.getAttribute("given_name");
        String familyName = oauth2User.getAttribute("family_name");

        if (email == null || subject == null) {
            logger.error("OAuth2 user missing email or sub");
            response.sendRedirect(frontendUrl + "/login?error=OAuth2MissingInfo");
            return;
        }

        email = email.toLowerCase().trim();

        // Check if user exists by Google Subject first
        Optional<User> userBySubject = userRepository.findByGoogleSubject(subject);
        User user;

        if (userBySubject.isPresent()) {
            user = userBySubject.get();
        } else {
            // Check by email
            Optional<User> userByEmail = userRepository.findByEmail(email);
            if (userByEmail.isPresent()) {
                user = userByEmail.get();
                if (user.getAuthProvider() == AuthProvider.LOCAL) {
                    // Account linking rule: Reject with clear error
                    logger.warn("User attempted Google login but has a LOCAL account: {}", email);
                    response.sendRedirect(frontendUrl + "/login?error=OAuth2AccountConflict");
                    return;
                } else {
                    // It's a Google account but missing the subject? Update subject.
                    user.setGoogleSubject(subject);
                    user = userRepository.save(user);
                }
            } else {
                // Create new Google User
                user = new User();
                user.setEmail(email);
                user.setFirstName(givenName != null ? givenName : "Unknown");
                user.setLastName(familyName != null ? familyName : "Unknown");
                
                // Generate dummy password for Google users to satisfy NOT NULL constraint
                byte[] dummyPasswordBytes = new byte[32];
                new SecureRandom().nextBytes(dummyPasswordBytes);
                String dummyPassword = Base64.getUrlEncoder().withoutPadding().encodeToString(dummyPasswordBytes);
                user.setPasswordHash(passwordEncoder.encode(dummyPassword));
                
                user.setRole(UserRole.CUSTOMER);
                user.setActive(true);
                user.setEmailVerified(true);
                user.setAuthProvider(AuthProvider.GOOGLE);
                user.setGoogleSubject(subject);
                
                user = userRepository.save(user);
                logger.info("Created new GOOGLE user: {}", email);
            }
        }

        // Generate one-time code
        String oneTimeCode = oauth2CodeService.generateCode(user.getId());

        // Redirect to frontend callback
        response.sendRedirect(frontendUrl + "/oauth2/callback?code=" + oneTimeCode);
    }
}
