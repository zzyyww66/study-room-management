package com.studyroom.server.security;

import com.studyroom.server.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@Order(1) // Ensure this filter runs early, but potentially after CORS or logging filters
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    // Define public paths that should not be protected by this filter
    // This set should ideally be managed via configuration
    private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/check-username/", // Needs to handle path variables
            "/api/auth/check-email/",    // Needs to handle path variables
            "/h2-console",              // H2 console, if enabled and public
            "/swagger-ui/", "/v3/api-docs/" // Swagger, if used
    ));

    // Define admin path prefixes or exact matches
    // Using simple string matching for this iteration. Regex or AntPathMatcher would be more robust.
    private static final Set<String> ADMIN_PATH_PATTERNS = new HashSet<>(Arrays.asList(
            "/api/users/active", // GET all active users
            "/api/users"         // GET paginated user list (exact match for the base, query params handle pagination)
            // Paths like /api/users/{userId}/status and /api/users/{userId} (DELETE) will be checked with startsWith and endsWith
    ));

    private static final String ROLE_ADMIN = "ADMIN";


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestURI = request.getRequestURI();
        logger.debug("JwtRequestFilter processing request: {}", requestURI);

        // Check if the path is public
        if (isPublicPath(requestURI)) {
            logger.debug("Path {} is public, skipping JWT validation.", requestURI);
            chain.doFilter(request, response);
            return;
        }

        // If the path is not public, it requires JWT authentication.
        logger.debug("Path {} is not public, proceeding with JWT validation.", requestURI);

        final String authorizationHeader = request.getHeader("Authorization");
        Long userId = null; // Changed from username to userId for request attribute
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                // Extract userId for request attribute.
                // jwtUtil.validateToken will be called later to confirm overall validity.
                userId = jwtUtil.extractUserId(jwt);
            } catch (IllegalArgumentException e) {
                logger.warn("Unable to get user ID from JWT: {}", e.getMessage());
            } catch (ExpiredJwtException e) {
                logger.warn("JWT Token has expired while extracting userId: {}", e.getMessage());
            } catch (SignatureException e) {
                logger.warn("JWT Signature validation failed while extracting userId: {}", e.getMessage());
            } catch (MalformedJwtException e) {
                logger.warn("JWT token is malformed while extracting userId: {}", e.getMessage());
            }
        } else {
            logger.warn("Authorization header does not start with Bearer String or is missing for protected path: {}", requestURI);
        }

        // Validate the token (checks signature, expiration, and well-formedness)
        // userId being non-null implies token was parseable to some extent.
        if (userId != null && jwt != null && jwtUtil.validateToken(jwt)) {
            logger.debug("JWT token is valid for userId {} accessing path {}", userId, requestURI);
            request.setAttribute("x-user-id", userId); // Set userId as a request attribute

            String role = jwtUtil.extractRole(jwt); // Extract role for all valid tokens
            if (role != null) {
                request.setAttribute("x-user-role", role); // Set role as a request attribute
                logger.debug("User role {} set as request attribute 'x-user-role'", role);
            } else {
                // This case should ideally not happen if token generation always includes a role.
                logger.warn("Role could not be extracted from JWT for userId {}. Role attribute not set.", userId);
            }

            // RBAC Check for Admin Paths
            if (isAdminPath(requestURI)) {
                logger.debug("Admin path {} accessed by user with role {}", requestURI, role);
                if (!ROLE_ADMIN.equals(role)) {
                    logger.warn("User {} with role {} attempted to access admin path {}. Sending 403 Forbidden.", userId, role, requestURI);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden: Access denied.");
                    return;
                }
                logger.debug("Admin user {} with role {} granted access to admin path {}.", userId, role, requestURI);
            }

            // If Spring Security context was being used (example):
            // String usernameForContext = jwtUtil.extractUsername(jwt); // Assuming username is needed for UserDetails
            // UserDetails userDetails = this.userDetailsService.loadUserByUsername(usernameForContext);
            // UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            //         userDetails, null, userDetails.getAuthorities());
            // authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } else {
            logger.warn("Invalid or missing JWT Token for protected path {}. Sending 401 Unauthorized.", requestURI);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Invalid or missing JWT token.");
        }
    }

    private boolean isPublicPath(String requestURI) {
        if (PUBLIC_PATHS.contains(requestURI)) {
            return true;
        }
        // Handle paths with path variables like /api/auth/check-username/{username}
        if (requestURI.startsWith("/api/auth/check-username/") || requestURI.startsWith("/api/auth/check-email/")) {
            return true;
        }
        if (requestURI.startsWith("/h2-console") || requestURI.startsWith("/swagger-ui/") || requestURI.startsWith("/v3/api-docs/")) {
             return true;
        }
        return false;
    }

    private boolean isAdminPath(String requestURI) {
        if (ADMIN_PATH_PATTERNS.contains(requestURI)) {
            return true;
        }
        // Check for patterns like /api/users/{userId}/status or /api/users/{userId} (DELETE)
        // A more robust solution would use regex or AntPathMatcher.
        if (requestURI.startsWith("/api/users/") && (requestURI.endsWith("/status") || request.getMethod().equals("DELETE"))) {
             // This is a simplified check. For DELETE, it assumes any DELETE to /api/users/{id} is admin.
             // For PUT to /status, it assumes any /api/users/{id}/status is admin.
            if (requestURI.matches("/api/users/[^/]+/status") || (request.getMethod().equals("DELETE") && requestURI.matches("/api/users/[^/]+"))) {
                 return true;
            }
        }
        // Note: /api/users/{userId}/profile (PUT) is NOT considered an admin path by default,
        // users should be able to update their own profile.
        // GET /api/users/{userId} is also not exclusively admin.
        return false;
    }
}
