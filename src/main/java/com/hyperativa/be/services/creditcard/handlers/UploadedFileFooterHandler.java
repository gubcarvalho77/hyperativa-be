package com.hyperativa.be.services.creditcard.handlers;

import com.hyperativa.be.dtos.UploadedFileDetails;
import org.springframework.stereotype.Component;

@Component
public class UploadedFileFooterHandler implements UploadedFileHandler<String, UploadedFileDetails> {
    @Override
    public UploadedFileDetails apply(String line) {
        if (line == null || line.length() < 51) {
            throw new IllegalArgumentException("invalid footer");
        }

        return new UploadedFileDetails(
                line.substring(0, 8).trim(),
                Integer.parseInt(line.substring(8, 14).trim())
        );
    }
}
