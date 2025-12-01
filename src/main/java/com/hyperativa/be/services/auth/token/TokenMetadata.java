package com.hyperativa.be.services.auth.token;

import java.time.Instant;

public record TokenMetadata(
        String username,
        Instant expiresAt,
        String type
) { }
