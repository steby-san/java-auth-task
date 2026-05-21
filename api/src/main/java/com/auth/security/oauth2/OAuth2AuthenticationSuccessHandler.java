package com.auth.security.oauth2;

import com.auth.model.User;
import com.auth.security.jwt.TokenProvider;
import com.auth.service.Dev2TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;
    private final Dev2TokenService dev2TokenService;

    @Value("${app.oauth2.authorized-redirect-uris}")
    private String[] authorizedRedirectUris;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;

        CustomOAuth2User customUser = (CustomOAuth2User) oauth2Token.getPrincipal();
        User user = customUser.getUser();

        log.info("OAuth2 success for user: {}", user.getEmail());

        String accessToken = tokenProvider.generateToken(user);
        String refreshToken = dev2TokenService.generateAndStoreRefreshToken(user);

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(request.isSecure()); // Chỉ gửi qua HTTPS
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
        response.addCookie(refreshCookie);

        // ✅ Redirect với access token
        String targetUrl = determineTargetUrl(request) + "?token=" + accessToken;
        log.info("Redirecting to: {}", targetUrl);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String determineTargetUrl(HttpServletRequest request) {
        String redirectUri = request.getParameter("redirect_uri");

        if (StringUtils.hasText(redirectUri) && isAuthorizedRedirectUri(redirectUri)) {
            return redirectUri;
        }
        return authorizedRedirectUris[0];
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        try {
            URI clientRedirectUri = URI.create(uri);
            return Arrays.stream(authorizedRedirectUris)
                    .anyMatch(authorizedUri -> {
                        URI authorized = URI.create(authorizedUri);
                        return authorized.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                                && authorized.getPort() == clientRedirectUri.getPort();
                    });
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}