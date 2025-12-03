package com.hyperativa.be.services.creditcard;

import com.hyperativa.be.dtos.CreditCardRequest;
import com.hyperativa.be.exceptions.ResourceExistsException;
import com.hyperativa.be.exceptions.ResourceNotFoundException;
import com.hyperativa.be.model.User;
import com.hyperativa.be.model.UserCreditCard;
import com.hyperativa.be.repositories.UserCreditCardRepository;
import com.hyperativa.be.repositories.UserRepository;
import com.hyperativa.be.services.creditcard.handlers.CreditCardHashHandler;
import com.hyperativa.be.util.LoggedUsernameSupplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCreditCardService {

    private final UserRepository userRepository;

    private final UserCreditCardRepository userCreditCardRepository;

    private final CreditCardCryptoService creditCardCryptoService;

    private final CreditCardHashHandler creditCardHashHandler;

    private final LoggedUsernameSupplier loggedUsernameSupplier;

    private final UnaryOperator<String> normalizedCardNumber = cardNumber ->
        cardNumber.replaceAll("[^0-9]", "");

    @Transactional
    public UUID registerCreditCard(final CreditCardRequest request) {

        final var username = loggedUsernameSupplier.get();

        log.info("Registering credit card for user: {}", username);

        var userCreditCard = buildCreditCard(
                UUID.randomUUID(),
                retrieveUser(username),
                request
        );

        log.info("Finished to register credit card for user: {}", username);

        return userCreditCardRepository.save(userCreditCard).getId();
    }

    @Transactional(readOnly = true)
    public UUID checkUserCreditCard(final CreditCardRequest request) {

        final var username = loggedUsernameSupplier.get();
        log.info("Start checking credit card for user - username={}", username);

        final var cardHash = creditCardHashHandler.apply(
                normalizedCardNumber.apply(request.cardNumber())
        );

        final var uuid = userCreditCardRepository.findIdByUsernameAndCardHash(username, cardHash)
                .orElseThrow(() -> {
                    log.warn("Credit card not found for user - username={}", username);
                    return new ResourceNotFoundException("User credit card not found");
                });

        log.info("Finished checking credit card for user - username={}", username);

        return uuid;
    }

    @Transactional
    public void registerAllCreditCards(
            final UUID transactionId,
            final List<CreditCardRequest> requests
    ) {
        final var username = loggedUsernameSupplier.get();

        log.info("Registering credit cards for user: {} - transactionId={}", username, transactionId);

        var user = retrieveUser(username);

        userCreditCardRepository.saveAll(
                requests.stream()
                        .map(request -> this.buildCreditCard(transactionId, user, request))
                        .toList()
        );

        log.info("Finished to register all credit cards for user: {} - transactionId={}", username, transactionId);
    }

    private User retrieveUser(final String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new ResourceNotFoundException("User not found: username=%s".formatted(username));
                });
    }

    private UserCreditCard buildCreditCard(
            final UUID transactionId,
            final User user,
            final CreditCardRequest request
    ) {
        var userCreditCard = new UserCreditCard();
        userCreditCard.setUser(user);
        userCreditCard.setTransactionId(transactionId);

        var cardNumber = normalizedCardNumber.apply(request.cardNumber());
        var cardHash = creditCardHashHandler.apply(cardNumber);

        if (userCreditCardRepository.findIdByUsernameAndCardHash(user.getUsername(), cardHash).isPresent()) {
            log.error("Credit card already exists for user: {}", user.getUsername());
            throw new ResourceExistsException(
                    "Credit card already exist for this user - username=%s cardHash=%s".formatted(
                            user.getUsername(),
                            cardHash
                    )
            );
        }

        userCreditCard.setCardHash(cardHash);
        userCreditCard.setLast4(cardNumber.substring(cardNumber.length() - 4));
        userCreditCard.setEncryptedCardNumber(creditCardCryptoService.encrypt(cardNumber));

        return userCreditCard;
    }

    @Transactional
    public void removeAllByTransactionId(
            final UUID transactionId
    ) {
        try {
            userCreditCardRepository.deleteByTransactionId(transactionId);
        } catch (Exception e) {
            log.error("Could not rollback transaction - transactionId={}", transactionId, e);
        }
    }
}
