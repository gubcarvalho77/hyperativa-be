package com.hyperativa.be.services.creditcard.handlers;

import com.hyperativa.be.dtos.CreditCardRequest;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UploadedFileLineHandler implements UploadedFileHandler<String, CreditCardRequest> {

    private final Validator validator;

    @Override
    public CreditCardRequest apply(String line) {
        if (line == null || !line.startsWith("C")) {
            throw new IllegalArgumentException("Invalid file content. Line should start with C");
        }

        if (line.length() < 26) {
            throw new IllegalArgumentException("Invalid file content. invalid line");
        }

        var creditCardRequest = new CreditCardRequest(line.substring(7, 26).trim());

        var violations = validator.validate(creditCardRequest);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Invalid file content");
        }

        return creditCardRequest;
    }
}
