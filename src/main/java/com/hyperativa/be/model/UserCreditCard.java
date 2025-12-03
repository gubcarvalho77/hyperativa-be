package com.hyperativa.be.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@ToString(callSuper = true, exclude = "user")
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@Entity
@Table(name="user_credit_card",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "card_hash"})
        }
)
@EntityListeners(AuditingEntityListener.class)
public class UserCreditCard extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "card_number_encrypted", nullable = false, length = 512)
    private String encryptedCardNumber;

    @Column(name = "card_hash", nullable = false, length = 64)
    private String cardHash;

    @Column(name = "card_last4", nullable = false, length = 4)
    private String last4;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;
}