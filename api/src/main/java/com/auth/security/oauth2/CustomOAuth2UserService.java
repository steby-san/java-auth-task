package com.auth.security.oauth2;

import com.auth.model.User;
import com.auth.model.enums.AuthProvider;
import com.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider authProvider = mapToAuthProvider(registrationId);

        String email = oAuth2User.getAttribute("email");
        String externalId = oAuth2User.getAttribute("sub");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(oAuth2User, authProvider, externalId, email));

        return new CustomOAuth2User(oAuth2User, user);
    }

    private User createNewUser(OAuth2User oAuth2User, AuthProvider authProvider,
                               String externalId, String email) {
        User user = User.builder()
                .id(UUID.randomUUID())  // ✅ Dùng UUID thay vì Long
                .email(email)
                .firstName(oAuth2User.getAttribute("given_name"))
                .lastName(oAuth2User.getAttribute("family_name"))
                .authProvider(authProvider)  // ✅ Dùng method builder
                .externalProviderId(externalId)  // ✅ Dùng method builder
                .isEnabled(true)
                .isAccountNonLocked(true)
                .build();

        log.info("Creating new user: {} with provider: {}", email, authProvider);
        return userRepository.save(user);
    }

    private AuthProvider mapToAuthProvider(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> AuthProvider.GOOGLE;
            case "facebook" -> AuthProvider.FACEBOOK;
            case "github" -> AuthProvider.GITHUB;
            default -> throw new OAuth2AuthenticationException(
                    "Unsupported OAuth2 provider: " + registrationId);
        };
    }
}