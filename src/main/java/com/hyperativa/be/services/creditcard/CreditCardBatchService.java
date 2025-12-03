package com.hyperativa.be.services.creditcard;

import com.hyperativa.be.dtos.CreditCardRequest;
import com.hyperativa.be.exceptions.CreditCardException;
import com.hyperativa.be.services.creditcard.handlers.UploadedFileFooterHandler;
import com.hyperativa.be.services.creditcard.handlers.UploadedFileHeaderHandler;
import com.hyperativa.be.services.creditcard.handlers.UploadedFileLineHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class CreditCardBatchService {

    private final int chunkSize;

    private final UserCreditCardService userCreditCardService;

    private final UploadedFileHeaderHandler headerHandler;

    private final UploadedFileLineHandler lineHandler;

    private final UploadedFileFooterHandler footerHandler;

    public CreditCardBatchService(
            @Value("${application.card.chunk-size:500}") int chunkSize,
            UserCreditCardService userCreditCardService,
            UploadedFileHeaderHandler headerHandler,
            UploadedFileLineHandler lineHandler,
            UploadedFileFooterHandler footerHandler
    ) {
        this.userCreditCardService = userCreditCardService;
        this.headerHandler = headerHandler;
        this.lineHandler = lineHandler;
        this.footerHandler = footerHandler;
        this.chunkSize = chunkSize <= 500
                ? chunkSize
                : 500;
    }

    public void processFile(
            MultipartFile file
    ) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        final var transactionId = UUID.randomUUID();

        log.info("Starting processing file: {}", file.getOriginalFilename());

        List<CreditCardRequest> buffer = new ArrayList<>(chunkSize);

        try (var reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            var headerLine = reader.readLine();
            var headerDetails = headerHandler.apply(headerLine);

            int processedCount = 0;
            String lastLine = null;

            String line;
            while ((line = reader.readLine()) != null) {

                if (line.startsWith("LOTE")) {
                    lastLine = line;
                    break;
                }

                buffer.add(lineHandler.apply(line));

                if (buffer.size() == chunkSize) {
                    saveChunk(transactionId, buffer);
                    processedCount += buffer.size();
                    buffer.clear();
                }
            }

            if (!buffer.isEmpty()) {
                saveChunk(transactionId, buffer);
                processedCount += buffer.size();
            }

            var footerDetails = footerHandler.apply(lastLine);

            if (!headerDetails.equals(footerDetails) || footerDetails.count() != processedCount) {
                throw new IllegalArgumentException("Invalid file content");
            }

            log.info("Finished processing file. Total processed: {}", processedCount);
        } catch (Exception e) {
            log.error("Unexpected error while processing file", e);
            userCreditCardService.removeAllByTransactionId(transactionId);
            throw new CreditCardException("Unexpected error while processing file", e);
        }
    }

    private void saveChunk(
            UUID transactionId,
            List<CreditCardRequest> chunk
    ) {
        userCreditCardService.registerAllCreditCards(transactionId, chunk);
    }
}
