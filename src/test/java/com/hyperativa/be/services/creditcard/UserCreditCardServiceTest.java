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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserCreditCardServiceTest {

    private UserRepository userRepository;

    private UserCreditCardRepository userCreditCardRepository;

    private CreditCardCryptoService cryptoService;

    private CreditCardHashHandler hashHandler;

    private LoggedUsernameSupplier loggedUsernameSupplier;

    private UserCreditCardService service;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        userCreditCardRepository = mock(UserCreditCardRepository.class);
        cryptoService = mock(CreditCardCryptoService.class);
        hashHandler = mock(CreditCardHashHandler.class);
        loggedUsernameSupplier = mock(LoggedUsernameSupplier.class);

        service = new UserCreditCardService(
                userRepository,
                userCreditCardRepository,
                cryptoService,
                hashHandler,
                loggedUsernameSupplier
        );
    }

    @Test
    void registerCreditCard_shouldSaveCard_whenUserExistsAndCardNew() {
        var username = "user1";
        when(loggedUsernameSupplier.get()).thenReturn(username);

        var user = new User();
        user.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        var cardNumber = "4111111111111111";
        var cardHash = "hash123";
        when(hashHandler.apply(cardNumber)).thenReturn(cardHash);
        when(cryptoService.encrypt(cardNumber)).thenReturn("encryptedCard");

        var savedCard = new UserCreditCard();
        var id = UUID.randomUUID();
        savedCard.setId(id);
        when(userCreditCardRepository.save(any())).thenReturn(savedCard);

        var result = service.registerCreditCard(new CreditCardRequest(cardNumber));

        assertThat(result).isEqualTo(id);

        var captor = ArgumentCaptor.forClass(UserCreditCard.class);
        verify(userCreditCardRepository).save(captor.capture());

        var saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getCardHash()).isEqualTo(cardHash);
        assertThat(saved.getEncryptedCardNumber()).isEqualTo("encryptedCard");
        assertThat(saved.getLast4()).isEqualTo("1111");
    }

    @Test
    void test_register_all_creditCard_ok() {
        var username = "user1";
        when(loggedUsernameSupplier.get()).thenReturn(username);

        var user = new User();
        user.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        var cardNumbers = List.of(
                new CreditCardRequest("4111111111111111")
        );

        when(hashHandler.apply(anyString())).thenReturn("hash123");
        when(cryptoService.encrypt(anyString())).thenReturn("encryptedCard");

        when(userCreditCardRepository.save(any())).thenReturn(new UserCreditCard());

        service.registerAllCreditCards(UUID.randomUUID(), cardNumbers);

        verify(userRepository).findByUsername(any());

        var arg = ArgumentCaptor.forClass(List.class);
        verify(userCreditCardRepository).saveAll(arg.capture());

        assertThat(arg.getValue()).hasSameSizeAs(cardNumbers);
    }

    @Test
    void registerCreditCard_shouldThrow_whenUserNotFound() {
        when(loggedUsernameSupplier.get()).thenReturn("user1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());

        var cardRequest = new CreditCardRequest("4111111111111111");

        assertThatThrownBy(() -> service.registerCreditCard(cardRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void registerCreditCard_shouldThrow_whenCardAlreadyExists() {

        var username = "user1";
        when(loggedUsernameSupplier.get()).thenReturn(username);

        var user = new User();
        user.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        var cardNumber = "4111111111111111";
        var cardHash = "hash123";
        when(hashHandler.apply(cardNumber)).thenReturn(cardHash);
        when(userCreditCardRepository.findIdByUsernameAndCardHash(username, cardHash))
                .thenReturn(Optional.of(UUID.randomUUID()));

        var cardRequest = new CreditCardRequest(cardNumber);

        assertThatThrownBy(() -> service.registerCreditCard(cardRequest))
                .isInstanceOf(ResourceExistsException.class)
                .hasMessageContaining("Credit card already exist");
    }

    @Test
    void checkUserCreditCard_shouldReturnUuid_whenCardExists() {
        var username = "user1";
        when(loggedUsernameSupplier.get()).thenReturn(username);

        var cardNumber = "4111111111111111";
        var cardHash = "hash123";
        when(hashHandler.apply(cardNumber)).thenReturn(cardHash);

        var id = UUID.randomUUID();
        when(userCreditCardRepository.findIdByUsernameAndCardHash(username, cardHash))
                .thenReturn(Optional.of(id));

        var result = service.checkUserCreditCard(new CreditCardRequest(cardNumber));
        assertThat(result).isEqualTo(id);
    }

    @Test
    void checkUserCreditCard_shouldThrow_whenCardNotFound() {
        var username = "user1";
        when(loggedUsernameSupplier.get()).thenReturn(username);

        var cardNumber = "4111111111111111";
        var cardHash = "hash123";
        when(hashHandler.apply(cardNumber)).thenReturn(cardHash);
        when(userCreditCardRepository.findIdByUsernameAndCardHash(username, cardHash))
                .thenReturn(Optional.empty());

        var cardRequest = new CreditCardRequest(cardNumber);

        assertThatThrownBy(() -> service.checkUserCreditCard(cardRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User credit card not found");
    }
}
