package com.hyperativa.be.services.creditcard;

import com.hyperativa.be.exceptions.CreditCardException;
import com.hyperativa.be.services.creditcard.handlers.UploadedFileFooterHandler;
import com.hyperativa.be.services.creditcard.handlers.UploadedFileHeaderHandler;
import com.hyperativa.be.services.creditcard.handlers.UploadedFileLineHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditCardBatchService {

    private final UserCreditCardService userCreditCardService;

    private final UploadedFileHeaderHandler headerHandler;

    private final UploadedFileLineHandler lineHandler;

    private final UploadedFileFooterHandler footerHandler;

    @Transactional
    public void processFile(MultipartFile file) {
        log.info("Starting processing file");

        try {
            extractCardLines(file).forEach(line -> {
                userCreditCardService.registerCreditCard(
                        lineHandler.apply(line)
                );
            });

            log.info("Finished processing file");
        } catch (Exception e) {
            log.error("Failed to process file");
            throw new CreditCardException("Unexpected error while processing file", e);
        }
    }

    private List<String> extractCardLines(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("file is empty");
        }

        List<String> lines;

        try (var reader = new BufferedReader(
                new InputStreamReader(file.getInputStream())
        )) {
            lines = reader.lines().toList();
        } catch (Exception e) {
            throw new IllegalArgumentException("Fail to read the file", e);
        }

        if (lines.size() < 3) {
            throw new IllegalArgumentException("Invalid file content");
        }

        var headerDetails = headerHandler.apply(lines.getFirst());
        var footerDetails = footerHandler.apply(lines.getLast());
        var cardLines = lines.subList(1, lines.size() - 1);

        if (!headerDetails.equals(footerDetails) || footerDetails.count() != cardLines.size()) {
            throw new IllegalArgumentException("Invalid file content");
        }

        return cardLines;
    }
}
