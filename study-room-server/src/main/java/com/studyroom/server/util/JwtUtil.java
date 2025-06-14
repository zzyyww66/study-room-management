package com.studyroom.server.util;

import com.studyroom.server.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret.key}")
    private String secretKey;

    private Key signingKey;

    // Token validity (e.g., 10 hours)
    private static final long TOKEN_VALIDITY = 10 * 60 * 60 * 1000; // 10 hours in milliseconds

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Generates a JWT token for a given user.
     *
     * @param user The user for whom the token is to be generated.
     * @return The generated JWT token.
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        if (user.getRole() != null) {
            claims.put("role", user.getRole().name()); // Add role to claims
        } else {
            claims.put("role", User.UserRole.USER.name()); // Default to USER if role is somehow null
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY))
                .signWith(this.signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the username from a JWT token.
     *
     * @param token The JWT token.
     * @return The username extracted from the token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the user ID from a JWT token.
     *
     * @param token The JWT token.
     * @return The user ID extracted from the token.
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Extracts the role from a JWT token.
     *
     * @param token The JWT token.
     * @return The role extracted from the token.
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token The JWT token.
     * @return The expiration date.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from a JWT token.
     *
     * @param token          The JWT token.
     * @param claimsResolver A function to resolve the desired claim.
     * @param <T>            The type of the claim.
     * @return The extracted claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses all claims from a JWT token.
     *
     * @param token The JWT token.
     * @return All claims from the token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(this.signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks if a JWT token has expired.
     *
     * @param token The JWT token.
     * @return True if the token has expired, false otherwise.
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validates a JWT token.
     * For simplicity, this example only checks the username and expiration.
     * A more robust validation would involve checking against user details from a database.
     *
     * @param token The JWT token.
     * @param user  The user details to validate against.
     * @return True if the token is valid, false otherwise.
     */
    public Boolean validateToken(String token, User user) {
        final String username = extractUsername(token);
        return (username.equals(user.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Validates a JWT token (without user details comparison, only checks expiration and signature).
     * Useful for scenarios where user details are not readily available or needed for validation.
     *
     * @param token The JWT token.
     * @return True if the token is structurally valid and not expired.
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            // Token parsing failed (e.g., malformed, signature mismatch)
            return false;
        }
    }
}
