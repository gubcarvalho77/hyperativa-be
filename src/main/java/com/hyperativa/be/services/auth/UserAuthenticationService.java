package com.hyperativa.be.services.auth;

import com.hyperativa.be.dtos.UserDetailsDTO;
import com.hyperativa.be.dtos.auth.AuthenticationRequest;
import com.hyperativa.be.dtos.auth.LoginResponse;
import com.hyperativa.be.services.auth.token.AbstractTokenService;
import com.hyperativa.be.services.auth.token.AccessTokenService;
import com.hyperativa.be.services.auth.token.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserAuthenticationService {

    private final AuthenticationManager authenticationManager;

    private final TokenService accessTokenService;

    public UserAuthenticationService(
            AuthenticationManager authenticationManager,
            @Qualifier("accessTokenService") TokenService accessTokenService
    ) {
        this.authenticationManager = authenticationManager;
        this.accessTokenService = accessTokenService;
    }

    public LoginResponse login(
            final AuthenticationRequest request
    ) {
        try {
            final var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

            return generateResponse(
                    (UserDetailsDTO) authentication.getPrincipal()
            );
        } catch (Exception e) {
            throw e instanceof AuthenticationException authenticationException
                    ? authenticationException
                    : new BadCredentialsException("Bad credentials", e);
        }
    }

    private LoginResponse generateResponse(
            UserDetailsDTO userDetails
    ) {
        final var username = userDetails.getUsername();

        final var accessToken = accessTokenService.generate(username);

        return new LoginResponse(
                accessToken,
                AbstractTokenService.TOKEN_PREFIX.trim(),
                AccessTokenService.ACCESS_TOKEN_EXPIRES_IN,
                null,
                new LoginResponse.UserInfo(
                        username,
                        userDetails.getFullName()
                )
        );
    }
}
