package com.ridelink.driver_service.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    public String extractUsername(String token) {
        return extractClaim(token, claims -> value(claims, "sub"));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> value(claims, "role"));
    }

    public String extractUserId(String token) {
        return extractClaim(token, claims -> value(claims, "userId"));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, claims -> {
            Object expiration = claims.get("exp");
            if (!(expiration instanceof Number)) {
                throw new IllegalArgumentException("Token has no valid expiration");
            }
            return Date.from(Instant.ofEpochSecond(((Number) expiration).longValue()));
        });
    }

    public <T> T extractClaim(String token, Function<Map<String, Object>, T> claimsResolver) {
        final Map<String, Object> claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Map<String, Object> extractAllClaims(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3 || !constantTimeEquals(parts[2], sign(parts[0] + "." + parts[1]))) {
            throw new IllegalArgumentException("Invalid JWT");
        }
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        try {
            return parseClaims(payload);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT payload", e);
        }
    }

    private static Map<String, Object> parseClaims(String json) {
        Map<String, Object> claims = new LinkedHashMap<>();
        String body = json.trim();
        if (!body.startsWith("{") || !body.endsWith("}")) {
            throw new IllegalArgumentException("Payload is not a JSON object");
        }
        body = body.substring(1, body.length() - 1).trim();
        if (body.isEmpty()) return claims;

        for (String member : body.split(",")) {
            String[] entry = member.split(":", 2);
            if (entry.length != 2) throw new IllegalArgumentException("Invalid claim");
            String name = unquote(entry[0].trim());
            String rawValue = entry[1].trim();
            if (rawValue.startsWith("\"") && rawValue.endsWith("\"")) {
                claims.put(name, unquote(rawValue));
            } else if (rawValue.matches("-?\\d+(\\.\\d+)?")) {
                claims.put(name, rawValue.contains(".")
                    ? Double.parseDouble(rawValue) : Long.parseLong(rawValue));
            } else if ("true".equals(rawValue) || "false".equals(rawValue)) {
                claims.put(name, Boolean.parseBoolean(rawValue));
            } else if ("null".equals(rawValue)) {
                claims.put(name, null);
            } else {
                throw new IllegalArgumentException("Invalid claim value");
            }
        }
        return claims;
    }

    private static String unquote(String value) {
        if (value.length() < 2 || value.charAt(0) != '"' || value.charAt(value.length() - 1) != '"') {
            throw new IllegalArgumentException("Invalid JSON string");
        }
        return value.substring(1, value.length() - 1)
            .replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(
                mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to verify JWT", e);
        }
    }

    private static boolean constantTimeEquals(String left, String right) {
        return java.security.MessageDigest.isEqual(left.getBytes(StandardCharsets.US_ASCII),
            right.getBytes(StandardCharsets.US_ASCII));
    }

    private static String value(Map<String, Object> claims, String name) {
        Object value = claims.get(name);
        return value == null ? null : value.toString();
    }

    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}