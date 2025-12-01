package com.hyperativa.be.dtos.auth;

import com.hyperativa.be.config.security.sanitization.SanitizingRequestDeserializer;

public class AuthenticationRequestDeserializer  extends SanitizingRequestDeserializer<AuthenticationRequest> {
    public AuthenticationRequestDeserializer() {
        super(AuthenticationRequest.class);
    }
}
