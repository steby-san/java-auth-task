package com.auth.security.oauth2;

import com.auth.security.CustomUserPrincipal;
import com.auth.service.JwtService;
import com.auth.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        CustomUserPrincipal principal =
                (CustomUserPrincipal) authentication.getPrincipal();

        String accessToken =
                jwtService.generateAccessToken(principal.getUser());

        String refreshToken =
                refreshTokenService.createRefreshToken(
                        principal.getUser(),
                        request.getRemoteAddr(),
                        request.getHeader("User-Agent")
                );

        Cookie refreshCookie = new Cookie(
                "refresh_token",
                refreshToken
        );

        refreshCookie.setHttpOnly(true);

        refreshCookie.setSecure(true);

        refreshCookie.setPath("/");

        refreshCookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(refreshCookie);

        String redirectUrl =
                frontendUrl + "/oauth2/redirect?token=" + accessToken;

        getRedirectStrategy().sendRedirect(
                request,
                response,
                redirectUrl
        );
    }
}