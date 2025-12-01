package com.hyperativa.be.services.creditcard;

import com.hyperativa.be.exceptions.CreditCardException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
public class CreditCardCryptoService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String ALGO = "AES/GCM/NoPadding";

    private static final int GCM_TAG_LENGTH = 128;

    private final SecretKey cryptoKey;

    public CreditCardCryptoService(
            @Value("${application.card.cryptoSecret}") String cryptoSecret
    ) {
        log.info("Initializing CreditCardCryptoService");

        try {
            var decoded = Base64.getDecoder().decode(cryptoSecret);
            if (decoded.length != 16 && decoded.length != 24 && decoded.length != 32) {
                log.error("Invalid AES key length: {}", decoded.length);
                throw new IllegalArgumentException("Invalid AES key length: " + decoded.length);
            }
            this.cryptoKey = new SecretKeySpec(decoded, "AES");
            log.info("AES key successfully initialized with size={} bytes", decoded.length);
        } catch (Exception e) {
            log.error("Error decoding AES key: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid Base64 AES key", e);
        }
    }

    public String encrypt(String cardNumber) {
        log.debug("encrypt called");

        try {
            if (StringUtils.trimToNull(cardNumber)  == null) {
                log.warn("Encrypt called with null or empty cardNumber");
                throw new IllegalArgumentException("Card number cannot be null or empty");
            }

            if (cardNumber.length() < 13 || cardNumber.length() > 19) {
                log.warn("Card number length out of range: length={}", cardNumber.length());
                throw new IllegalArgumentException("Card number must contain between 13 and 19 digits");
            }

            var iv = new byte[12];
            RANDOM.nextBytes(iv);

            log.debug("Generated IV of {} bytes", iv.length);

            var cipher = createCipher(iv, Cipher.ENCRYPT_MODE);
            var encrypted = cipher.doFinal(cardNumber.getBytes(StandardCharsets.UTF_8));

            log.debug("Credit card encrypted successfully");

            return Base64.getEncoder().encodeToString(iv) + ":" +
                    Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Error encrypting card: {}", e.getMessage());
            throw new CreditCardException("Error encrypting card: " + e.getMessage(), e);
        }
    }

    public String decrypt(String encrypted) {
        log.debug("decrypt called");

        try {
            var parts = encrypted.split(":");
            if (parts.length != 2) {
                log.warn("Encrypted string format invalid");
                throw new IllegalArgumentException("Invalid encrypted format");
            }

            var iv = Base64.getDecoder().decode(parts[0]);
            var cipherText = Base64.getDecoder().decode(parts[1]);

            log.debug("Decrypting with IV size={} bytes, cipherText size={} bytes", iv.length, cipherText.length);

            var cipher = createCipher(iv, Cipher.DECRYPT_MODE);

            var plain = new String(
                    cipher.doFinal(cipherText),
                    StandardCharsets.UTF_8
            );

            log.info("Credit card decrypted successfully");
            return plain;
        } catch (Exception e) {
            throw new CreditCardException("Error decrypting card", e);
        }
    }

    protected Cipher createCipher(byte[] iv, int mode)
            throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException
    {
        log.debug("Creating cipher with mode={} ivLength={}", mode, iv.length);

        var cipher = Cipher.getInstance(ALGO);
        var spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        cipher.init(mode, cryptoKey, spec);

        return cipher;
    }
}

