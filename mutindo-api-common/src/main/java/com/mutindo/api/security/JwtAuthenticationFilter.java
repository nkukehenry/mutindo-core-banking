package com.mutindo.api.security;

import com.mutindo.common.context.BranchContext; // Reusing existing branch context
import com.mutindo.common.context.BranchContextHolder; // Reusing existing context holder
import com.mutindo.common.enums.UserType;
import com.mutindo.jwt.dto.JwtClaims; // Reusing existing JWT infrastructure
import com.mutindo.jwt.service.IJwtService; // Reusing existing JWT service interface
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter - reuses existing JWT infrastructure
 * Extracts JWT token, validates it, and sets security context
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final IJwtService jwtService; // Reusing existing JWT service interface
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extract JWT token from header (small method)
            String token = extractTokenFromRequest(request);
            
            if (token != null && !token.isEmpty()) {
                // Validate token and extract claims (reusing existing JWT service)
                authenticateUser(token);
            }
            
        } catch (Exception e) {
            log.warn("JWT authentication failed: {}", e.getMessage());
            // Continue filter chain even if authentication fails
            // Let Spring Security handle unauthorized access
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }

    /**
     * Authenticate user and set security context
     */
    private void authenticateUser(String token) {
        try {
            // Validate token and extract claims (reusing existing JWT service)
            JwtClaims claims = jwtService.validateAndExtractClaims(token);
            
            // Set branch context (reusing existing context infrastructure)
            setBranchContext(claims);
            
            // Set Spring Security context (small method)
            setSecurityContext(claims);
            
            log.debug("User authenticated successfully: {} - Branch: {}", 
                    claims.getUserId(), claims.getBranchId());
            
        } catch (Exception e) {
            log.warn("Failed to authenticate user with JWT", e);
            throw e; // Re-throw to be handled by caller
        }
    }

    /**
     * Set branch context for multi-branch operations
     */
    private void setBranchContext(JwtClaims claims) {
        BranchContext branchContext = BranchContext.builder()
                .userId(claims.getUserId())
                .branchId(claims.getBranchId()) // null for institution admins
                .userType(UserType.valueOf(claims.getUserType()))
                .institutionId("DEFAULT") // Would come from claims in multi-tenant setup
                .build();
        
        BranchContextHolder.setContext(branchContext);
    }

    /**
     * Set Spring Security context with user authorities
     */
    private void setSecurityContext(JwtClaims claims) {
        // Convert permissions to Spring Security authorities
        List<SimpleGrantedAuthority> authorities = claims.getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority("ROLE_" + permission.replace(":", "_").toUpperCase()))
                .collect(Collectors.toList());
        
        // Add roles as authorities
        List<SimpleGrantedAuthority> roleAuthorities = claims.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
        
        authorities.addAll(roleAuthorities);
        
        // Create authentication token
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(claims.getUserId(), null, authorities);
        
        // Set in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Skip JWT authentication for public endpoints
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Skip authentication for public endpoints
        return path.startsWith("/api/v1/auth/login") ||
               path.startsWith("/api/v1/auth/refresh") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/actuator/health");
    }
}
