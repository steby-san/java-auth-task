package com.auth.service;

import com.auth.model.AuthProvider;
import com.auth.model.User;
import com.auth.repository.UserRepository;
import com.auth.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId =
                userRequest.getClientRegistration().getRegistrationId();

        Map<String, Object> attributes = oauth2User.getAttributes();

        String providerId;
        String email;
        String name;

        if ("google".equals(registrationId)) {

            providerId = (String) attributes.get("sub");
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");

        } else if ("facebook".equals(registrationId)) {

            providerId = String.valueOf(attributes.get("id"));
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");

        } else {
            throw new OAuth2AuthenticationException("Unsupported provider");
        }

        User user = userRepository
                .findByEmail(email)
                .orElseGet(() -> createOAuthUser(
                        email,
                        name,
                        providerId,
                        registrationId
                ));

        return new CustomUserPrincipal(
                user,
                attributes,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    private User createOAuthUser(
            String email,
            String name,
            String providerId,
            String provider
    ) {

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .email(email)
                .firstName(name)
                .provider(
                        "google".equals(provider)
                                ? AuthProvider.GOOGLE
                                : AuthProvider.FACEBOOK
                )
                .providerId(providerId)
                .enabled(true)
                .accountNonLocked(true)
                .build();

        return userRepository.save(user);
    }
}