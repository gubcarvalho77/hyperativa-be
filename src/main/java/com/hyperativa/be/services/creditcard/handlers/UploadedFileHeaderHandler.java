package com.hyperativa.be.services.creditcard.handlers;

import com.hyperativa.be.dtos.UploadedFileDetails;
import org.springframework.stereotype.Component;

@Component
public class UploadedFileHeaderHandler implements UploadedFileHandler<String, UploadedFileDetails> {

    @Override
    public UploadedFileDetails apply(String line) {
        if (line.length() < 51) {
            throw new IllegalArgumentException("invalid header");
        }

        return new UploadedFileDetails(
                line.substring(37, 45).trim(),
                Integer.parseInt(line.substring(45, 51).trim())
        );
    }
}
