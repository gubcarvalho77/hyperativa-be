package com.hyperativa.be.validators;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreditCardValidatorTest {

    private final CreditCardValidator validator = new CreditCardValidator();

    @ParameterizedTest
    @ValueSource(strings = {
            "4111111111111111",
            "4111 1111 1111 1111",
            "4111-1111-1111-1111",
            "5555 5555 5555 4444",
            "3782 822463 10005",
            "6011 1111 1111 1117"
    })
    void test_valid_values(
            String cardNumber
    ) {
        assertTrue(validator.isValid(cardNumber, null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "4111-1111-1111-111X",
            "4111 1111 1111 111",
            "abcdefg123",
            "411111111111112",
            "1234"
    })
    void test_invalid_values(
            String cardNumber
    ) {
        assertFalse(validator.isValid(cardNumber, null));
    }
}