package com.auth.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Getter // Lombok auto-generates getPublicKey(), getPrivateKey()
@SuppressWarnings("unused") // Remove this annotation after integrating with AuthController
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private static final long ACCESS_TOKEN_EXPIRATION_MS = 15L * 60L * 1000L;

    public JwtUtils() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        this.privateKey = (RSAPrivateKey) kp.getPrivate();
        this.publicKey = (RSAPublicKey) kp.getPublic();
    }

    public String generateToken(String email, Collection<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_MS);
        return Jwts.builder()
                .subject(email)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.debug("Token expired");
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.warn("Invalid signature");
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("Malformed token");
        } catch (Exception e) {
            log.debug("Validation failed: {}", e.getClass().getSimpleName());
        }
        return false;
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?>) {
            return ((List<?>) rolesObj).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}