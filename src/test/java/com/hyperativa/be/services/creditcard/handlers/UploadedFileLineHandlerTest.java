package com.hyperativa.be.services.creditcard.handlers;

import com.hyperativa.be.dtos.CreditCardRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadedFileLineHandlerTest {

    @Mock
    private Validator validator;

    @InjectMocks
    private UploadedFileLineHandler handler;

    @Test
    void apply_shouldReturnCreditCardRequest_whenLineIsValid() {

        var line = "C1     4456897999999999      ";
        when(validator.validate(any(CreditCardRequest.class))).thenReturn(Collections.emptySet());

        var result = handler.apply(line);

        assertThat(result).isNotNull();
        assertThat(result.cardNumber()).isEqualTo("4456897999999999");

        verify(validator).validate(result);
    }

    @Test
    void apply_shouldThrowException_whenLineDoesNotStartWithC() {
        var line = "X1     4456897999999999      ";
        assertThatThrownBy(() -> handler.apply(line))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("should start with C");
    }

    @Test
    void apply_shouldThrowException_whenLineIsTooShort() {
        var line = "C1 12345";
        assertThatThrownBy(() -> handler.apply(line))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid line");
    }

    @Test
    void apply_shouldThrowException_whenValidationFails() {
        var line = "C1     4456897999999999      ";

        ConstraintViolation<CreditCardRequest> violation = mock(ConstraintViolation.class);
        lenient().when(violation.getMessage()).thenReturn("Invalid card");

        Set<ConstraintViolation<CreditCardRequest>> violations = Set.of(violation);
        lenient().when(validator.validate(any(CreditCardRequest.class))).thenReturn(violations);

        assertThatThrownBy(() -> handler.apply(line))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid file content");
    }
}
