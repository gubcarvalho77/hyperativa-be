package com.hyperativa.be.dtos.auth;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@JsonDeserialize(using = AuthenticationRequestDeserializer.class)
public record AuthenticationRequest(
        @Parameter(description = "User email")
        @Email @NotBlank String email,
        @Parameter(description = "User password")
        @NotBlank String password
) { }
