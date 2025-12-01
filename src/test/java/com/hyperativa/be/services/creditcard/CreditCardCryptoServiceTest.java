package com.hyperativa.be.services.creditcard;

import com.hyperativa.be.exceptions.CreditCardException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class CreditCardCryptoServiceTest {

    private CreditCardCryptoService cryptoService;

    @BeforeEach
    void setup() throws NoSuchAlgorithmException {
        var secret = Base64.getEncoder().encodeToString(
                KeyGenerator.getInstance("AES").generateKey().getEncoded()
        );
        cryptoService = new CreditCardCryptoService(secret);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "4111111111111111",
            "5555444433332222"
    })
    void test_encrypt_decrypt(
            String card
    ) {
        final var encrypted = cryptoService.encrypt(card);

        assertThat(encrypted)
                .isNotEqualTo(card)
                .contains(":");

        var decrypted = cryptoService.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(card);
    }

    @Test
    void test_different_ciphers_for_same_input() {
        var card = "4111111111111111";

        var encrypted1 = cryptoService.encrypt(card);
        var encrypted2 = cryptoService.encrypt(card);

        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    void test_shouldThrowException_whenKeyLengthInvalid() {
        var invalidBytes = new byte[10];
        var invalidSecret = Base64.getEncoder().encodeToString(invalidBytes);

        assertThatThrownBy(() -> new CreditCardCryptoService(invalidSecret))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Base64 AES key");
    }

    @ParameterizedTest
    @CsvSource(value = {
            ",Card number cannot be null or empty",
            "AAA12121,Card number must contain between 13 and 19 digits"
    })
    void encrypt_should_fail_with_invalid_value(
            String cardNumber,
            String expectedMessage
    ) {
        assertThatThrownBy(() -> cryptoService.encrypt(cardNumber))
                .isInstanceOf(CreditCardException.class)
                .hasMessageContaining(expectedMessage);
    }

    @Test
    void decrypt_should_fail_with_invalid_value() {
        var invalid = "INVALID_VALUE";

        assertThatThrownBy(() -> cryptoService.decrypt(invalid))
                .isInstanceOf(CreditCardException.class)
                .hasMessageContaining("Error decrypting card");
    }
}