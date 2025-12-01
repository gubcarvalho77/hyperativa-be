package com.hyperativa.be.dtos;

import com.hyperativa.be.validators.ValidCreditCard;
import io.swagger.v3.oas.annotations.Parameter;

public record CreditCardRequest(
        @Parameter(description = "Credit card number")
        @ValidCreditCard String cardNumber
) { }
