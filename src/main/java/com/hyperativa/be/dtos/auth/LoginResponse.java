package com.hyperativa.be.dtos.auth;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Long expiresIn,
        String refreshToken,
        UserInfo user
) {
    public record UserInfo(
            String id,
            String name
    ) {}
}
