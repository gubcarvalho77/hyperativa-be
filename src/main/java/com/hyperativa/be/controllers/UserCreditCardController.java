package com.hyperativa.be.controllers;

import com.hyperativa.be.dtos.CreditCardRequest;
import com.hyperativa.be.exceptions.ValidationException;
import com.hyperativa.be.services.creditcard.CreditCardBatchService;
import com.hyperativa.be.services.creditcard.UserCreditCardService;
import com.hyperativa.be.util.LoggedUsernameSupplier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/credit-cards")
@RequiredArgsConstructor
@Tag(name = "Credit Cards", description = "Operations related to user credit cards")
public class UserCreditCardController {

    private final LoggedUsernameSupplier loggedUsernameSupplier;

    private final UserCreditCardService userCreditCardService;

    private final CreditCardBatchService creditCardBatchService;

    @Operation(
            summary = "Add a new credit card for the logged-in user",
            description = "Registers a new credit card for the currently logged-in user",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Credit card successfully added"),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "409", description = "Credit card already exists")
            }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID addCreditCard(
            @Parameter(description = "Credit card data")
            @Valid @RequestBody CreditCardRequest request
    ) {
        var username = loggedUsernameSupplier.get();
        log.info("User {} adding a credit card", username);

        var cardId = userCreditCardService.registerCreditCard(request);

        log.info("User {} successfully added credit card with ID {}", username, cardId);
        return cardId;
    }

    @Operation(
            summary = "Check if a credit card is registered",
            description = "Checks whether the provided credit card is already registered for the logged-in user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Credit card found"),
                    @ApiResponse(responseCode = "404", description = "Credit card not found")
            }
    )
    @PostMapping("/check")
    public UUID findCreditCard(
            @Parameter(description = "Credit card data")
            @Valid @RequestBody CreditCardRequest request
    ) {
        var username = loggedUsernameSupplier.get();
        log.info("User {} checking credit card", username);

        var cardId = userCreditCardService.checkUserCreditCard(request);

        log.info("User {} credit card check successful: {}", username, cardId);
        return cardId;
    }

    @Operation(
            summary = "Upload a batch file with credit cards",
            description = "Uploads a file containing multiple credit cards to be registered in batch",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Batch file processed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid or empty file")
            }
    )
    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public void uploadBatch(
            @Parameter(description = "File containing credit cards")
            @RequestParam("file") MultipartFile file
    ) {
        var username = loggedUsernameSupplier.get();
        log.info("User {} uploading batch file: {}", username, file.getOriginalFilename());

        if (file.isEmpty()) {
            log.warn("User {} attempted to upload empty file", username);
            throw new ValidationException("File is empty");
        }

        creditCardBatchService.processFile(file);

        log.info("User {} successfully uploaded batch file: {}", username, file.getOriginalFilename());
    }
}
