package com.hyperativa.be.controllers;

import com.hyperativa.be.dtos.auth.AuthenticationRequest;
import com.hyperativa.be.dtos.auth.LoginResponse;
import com.hyperativa.be.services.auth.UserAuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(AuthenticationController.AUTH_BASE_URL)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and login")
public class AuthenticationController {

    public static final String AUTH_BASE_URL = "auth";

    private final UserAuthenticationService userAuthenticationService;

    @Operation(
            summary = "User login",
            description = "Authenticate user with username and password",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                    @ApiResponse(responseCode = "400", description = "Validation error")
            }
    )
    @PostMapping("/login")
    public LoginResponse login(
            @Parameter(description = "User authentication data")
            @Valid @RequestBody AuthenticationRequest request
    ) {
        return userAuthenticationService.login(request);
    }
}
