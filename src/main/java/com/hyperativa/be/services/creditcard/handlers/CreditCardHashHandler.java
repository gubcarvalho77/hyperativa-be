package com.hyperativa.be.services.creditcard.handlers;

import com.hyperativa.be.exceptions.CreditCardException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.UnaryOperator;

@Component
public class CreditCardHashHandler implements UnaryOperator<String> {

    private static final String HASH_ALGORITHM = "HmacSHA256";

    private final SecretKey key;

    private final ThreadLocal<Mac> threadLocalMac = ThreadLocal.withInitial(() -> {
        try {
            return Mac.getInstance(HASH_ALGORITHM);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize Mac", e);
        }
    });

    public CreditCardHashHandler(
            @Value("${application.card.hashSecret}") String hashSecret
    ) {
        this.key = new SecretKeySpec(
                hashSecret.getBytes(StandardCharsets.UTF_8),
                HASH_ALGORITHM
        );
    }

    @Override
    public String apply(@NonNull String normalizedCardNumber) {
        var mac = threadLocalMac.get();

        try {
            mac.reset();
            mac.init(this.key);

            var result = mac.doFinal(normalizedCardNumber.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new CreditCardException("Error hashing card", e);
        } finally {
            threadLocalMac.remove();
        }
    }
}
