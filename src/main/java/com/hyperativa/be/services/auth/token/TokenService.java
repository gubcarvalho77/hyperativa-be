package com.hyperativa.be.services.auth.token;

public interface TokenService {

    String generate(String username);

    TokenMetadata validate(String token);
}
