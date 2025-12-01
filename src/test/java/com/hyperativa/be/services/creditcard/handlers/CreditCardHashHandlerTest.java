package com.hyperativa.be.services.creditcard.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class CreditCardHashHandlerTest {

    private static final String SECRET = "any-test-secret-key-123";

    private CreditCardHashHandler hashHandler;

    @BeforeEach
    void setup() {
        hashHandler = new CreditCardHashHandler(SECRET);
    }

    @Test
    void should_generate_same_hash_for_same_input() {
        var card = "4111111111111111";

        var hash1 = hashHandler.apply(card);
        var hash2 = hashHandler.apply(card);

        assertThat(hash1)
                .isNotNull()
                .isEqualTo(hash2);
    }

    @Test
    void should_generate_different_hashes_for_different_inputs() {
        var card1 = "4111111111111111";
        var card2 = "5555555555554444";

        var hash1 = hashHandler.apply(card1);
        var hash2 = hashHandler.apply(card2);

        assertThat(hash1)
                .isNotNull()
                .isNotEqualTo(hash2);
    }

    @Test
    void should_be_thread_safe() throws Exception {
        var card = "4111111111111111";

        ExecutorService executor = Executors.newFixedThreadPool(10);

        Callable<String> task = () -> hashHandler.apply(card);

        var futures = executor.invokeAll(
                java.util.stream.Stream.generate(() -> task)
                        .limit(50)
                        .toList()
        );

        executor.shutdown();

        var results = futures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        assertThat(results).isNotEmpty();
        assertThat(results.stream().distinct().count()).isEqualTo(1);
    }
}