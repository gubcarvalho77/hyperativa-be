package com.hyperativa.be.services.auth.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.hyperativa.be.config.security.SecurityProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
public abstract class AbstractTokenService implements TokenService {

    public static final String TOKEN_PREFIX = "Bearer ";

    private final String issuer;

    private final String scope;

    private final String secret;

    private final Duration ttl;

    private final Function<Duration, Instant> expires = duration ->
            ZonedDateTime.now(ZoneOffset.UTC)
                    .plus(duration)
                    .toInstant();

    protected AbstractTokenService(
            String issuer,
            String scope,
            SecurityProperties securityProperties
    ) {
        this.scope = scope;
        this.issuer = issuer;

        var settings = securityProperties.tokenSettings().get(scope);
        this.secret = settings.secret();
        this.ttl = Duration.ofSeconds(
                Optional.ofNullable(settings.ttl())
                        .orElse(300L)
        );
    }

    @Override
    public String generate(
            @NonNull String username
    ) {
        var jti = UUID.randomUUID().toString();

        return JWT.create()
                .withIssuer(issuer)
                .withJWTId(jti)
                .withClaim("type", scope)
                .withIssuedAt(Instant.now())
                .withExpiresAt(expires.apply(this.ttl))
                .withSubject(username)
                .sign(getAlgorithm());
    }

    @Override
    public TokenMetadata validate(final String token) {
        try {
            final var decodedJWT = JWT.require(getAlgorithm())
                    .withIssuer(issuer)
                    .withClaim("type", scope)
                    .build()
                    .verify(token);

            final var jti = decodedJWT.getId();
            if (jti == null || jti.isBlank()) {
                throw new AccessDeniedException("Token without jti");
            }

            return new TokenMetadata(
                    decodedJWT.getSubject(),
                    decodedJWT.getExpiresAt() != null
                            ? decodedJWT.getExpiresAt().toInstant()
                            : null,
                    decodedJWT.getClaim("type").asString()
            );
        } catch (JWTVerificationException e) {
            throw new AccessDeniedException("Invalid or expired token", e);
        }
    }

    protected Algorithm getAlgorithm() {
        return Algorithm.HMAC256(secret);
    }
}
