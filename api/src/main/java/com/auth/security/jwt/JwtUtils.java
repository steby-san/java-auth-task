package com.auth.security.jwt;

import com.auth.model.Role;
import com.auth.model.User;
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
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Getter
public class JwtUtils implements TokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);
    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private static final long ACCESS_TOKEN_EXPIRATION_MS = 15L * 60L * 1000L; // 15 phút

    /**
     * Constructor khởi tạo RSA KeyPair
     * ⚠️ Lưu ý: Trong production, nên lưu key vào file/config, không tạo mới mỗi lần khởi động
     */
    public JwtUtils() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        this.privateKey = (RSAPrivateKey) kp.getPrivate();
        this.publicKey = (RSAPublicKey) kp.getPublic();
        log.info("✅ RSA KeyPair generated successfully");
    }

    /**
     * ✅ Generate token từ User entity (dùng trong OAuth2, Login)
     */
    @Override
    public String generateToken(User user) {
        // Convert Set<Role> → Collection<String> (chỉ lấy roleCode)
        Collection<String> roleCodes = user.getRoles().stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());
        return generateToken(user.getEmail(), roleCodes);
    }

    /**
     * ✅ Generate token từ email và danh sách roles (dạng String)
     */
    public String generateToken(String email, Collection<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_MS);

        return Jwts.builder()
                .subject(email)                           // Đặt email làm subject
                .claim("roles", roles)                    // Thêm roles vào claim
                .issuedAt(now)                            // Thời điểm phát hành
                .expiration(expiry)                       // Thời điểm hết hạn
                .signWith(privateKey, Jwts.SIG.RS256)    // Ký bằng RSA private key
                .compact();                               // Build token
    }

    /**
     * ✅ Validate token: kiểm tra chữ ký và hạn sử dụng
     */
    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(publicKey)                // Verify bằng public key
                    .build()
                    .parseSignedClaims(token);            // Parse và validate
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.debug("❌ Token expired: {}", e.getMessage());
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.warn("❌ Invalid signature: {}", e.getMessage());
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("❌ Malformed token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("❌ Empty or null token");
        } catch (Exception e) {
            log.debug("❌ Validation failed: {} - {}", e.getClass().getSimpleName(), e.getMessage());
        }
        return false;
    }

    /**
     * ✅ Extract email từ token
     */
    @Override
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * ✅ Extract roles từ token (dạng List<String>)
     */
    @Override
    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?>) {
            return ((List<?>) rolesObj).stream()
                    .filter(obj -> obj != null)
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return List.of(); // Trả về list rỗng nếu không có roles
    }
}