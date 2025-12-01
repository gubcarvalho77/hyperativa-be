package com.hyperativa.be.config.security;

import com.hyperativa.be.services.auth.AuthenticationService;
import com.hyperativa.be.services.auth.token.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

import static com.hyperativa.be.services.auth.token.AbstractTokenService.TOKEN_PREFIX;

@Order(1)
@Slf4j
@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService accessTokenService;

    private final AuthenticationService authenticationService;

    public SecurityFilter(
            AuthenticationService authenticationService,
            @Qualifier("accessTokenService") TokenService accessTokenService
    ) {
        this.accessTokenService = accessTokenService;
        this.authenticationService = authenticationService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final var token = this.extractToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                final var metadata = accessTokenService.validate(token);
                if (metadata != null) {
                    var userDetails = authenticationService.loadUserByUsername(metadata.username());

                    if (userDetails != null) {
                        final var authentication = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authentication.setDetails(metadata);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
//            } catch (TokenExpiredException ex) {
//                log.debug("Token expired: {}", ex.getMessage());
//            } catch (InvalidCsrfTokenException ex) {
//                log.debug("Invalid token: {}", ex.getMessage());
//            } catch (Exception ex) {
//                log.warn("Unexpected error during token authentication", ex);
//            }
            } catch (Exception ex) {
                if (log.isDebugEnabled()) {
                    log.debug("Token authentication failed: {}", ex.getMessage());
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request)     {
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(header -> header.startsWith(TOKEN_PREFIX))
                .map(header -> header.replace(TOKEN_PREFIX, ""))
                .orElse(null);
    }
}
