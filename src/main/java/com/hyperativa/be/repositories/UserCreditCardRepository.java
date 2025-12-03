package com.hyperativa.be.repositories;

import com.hyperativa.be.model.UserCreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCreditCardRepository extends JpaRepository<UserCreditCard, UUID> {

    @Query("""
        SELECT ucc.id 
        FROM UserCreditCard ucc
        WHERE ucc.user.username = :username
          AND ucc.cardHash = :cardHash
    """)
    Optional<UUID> findIdByUsernameAndCardHash(String username, String cardHash);

    void deleteByTransactionId(UUID transactionId);
}
