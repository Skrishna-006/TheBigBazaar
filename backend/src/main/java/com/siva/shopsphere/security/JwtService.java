package com.siva.shopsphere.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.siva.shopsphere.accounts.entity.User;

@Service
public class JwtService {

    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    private final byte[] secretBytes;
    private final long accessTokenExpirationMillis;
    private final long refreshTokenExpirationMillis;

    public JwtService(
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.access-expiration-ms}") long accessTokenExpirationMillis,
        @Value("${app.jwt.refresh-expiration-ms}") long refreshTokenExpirationMillis
    ) {
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.accessTokenExpirationMillis = accessTokenExpirationMillis;
        this.refreshTokenExpirationMillis = refreshTokenExpirationMillis;
    }

    public String generateAccessToken(User user) {
        return generateToken(user, "access", accessTokenExpirationMillis);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, "refresh", refreshTokenExpirationMillis);
    }

    public String extractSubject(String token) {
        return claims(token).get("sub");
    }

    public String extractRole(String token) {
        return claims(token).get("role");
    }

    public boolean isAccessTokenValid(String token) {
        return isTokenValid(token, "access");
    }

    public boolean isRefreshTokenValid(String token) {
        return isTokenValid(token, "refresh");
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMillis / 1000;
    }

    private boolean isTokenValid(String token, String expectedType) {
        try {
            Map<String, String> claims = claims(token);
            long exp = Long.parseLong(claims.get("exp"));
            return expectedType.equals(claims.get("typ")) && Instant.now().toEpochMilli() < exp;
        } catch (Exception ex) {
            return false;
        }
    }

    private String generateToken(User user, String type, long expirationMillis) {
        Instant now = Instant.now();
        long iat = now.toEpochMilli();
        long exp = now.plusMillis(expirationMillis).toEpochMilli();
        String identifier = user.getPhoneNumber() != null ? user.getPhoneNumber() : user.getEmail();
        String payload = json(Map.of(
            "sub", identifier,
            "iat", iat,
            "exp", exp,
            "role", user.getRole().name(),
            "typ", type
        ));
        String header = base64Url(HEADER_JSON.getBytes(StandardCharsets.UTF_8));
        String body = base64Url(payload.getBytes(StandardCharsets.UTF_8));
        String signature = sign(header + "." + body);
        return header + "." + body + "." + signature;
    }

    private Map<String, String> claims(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Malformed token");
        }
        String signingInput = parts[0] + "." + parts[1];
        if (!MessageDigest.isEqual(signingInput.getBytes(StandardCharsets.UTF_8), signingInput.getBytes(StandardCharsets.UTF_8))) {
            // no-op guard to keep constant-time pattern structure simple
        }
        if (!sign(parts[0] + "." + parts[1]).equals(parts[2])) {
            throw new IllegalArgumentException("Invalid signature");
        }
        String json = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        return parseFlatJson(json);
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
            return base64Url(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign JWT", ex);
        }
    }

    private String base64Url(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private String json(Map<String, Object> values) {
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('"').append(entry.getKey()).append('"').append(':');
            Object value = entry.getValue();
            if (value instanceof Number) {
                builder.append(value);
            } else {
                builder.append('"').append(escape(value.toString())).append('"');
            }
        }
        builder.append('}');
        return builder.toString();
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private Map<String, String> parseFlatJson(String json) {
        Map<String, String> values = new LinkedHashMap<>();
        String trimmed = json.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            throw new IllegalArgumentException("Invalid token payload");
        }
        String body = trimmed.substring(1, trimmed.length() - 1).trim();
        if (body.isEmpty()) {
            return values;
        }
        for (String pair : body.split(",")) {
            String[] kv = pair.split(":", 2);
            if (kv.length != 2) {
                throw new IllegalArgumentException("Invalid token payload");
            }
            String key = unquote(kv[0].trim());
            String rawValue = kv[1].trim();
            values.put(key, rawValue.startsWith("\"") ? unquote(rawValue) : rawValue);
        }
        return values;
    }

    private String unquote(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return trimmed;
    }
}
