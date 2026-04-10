package com.nexus.banking.transaction_service.common.repository;

import com.nexus.banking.transaction_service.common.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionReference(String reference);

    Page<Transaction> findByInitiatedByOrderByCreatedAtDesc(String initiatedBy, Pageable pageable);

    /**
     * Finds every transaction that touched this account (as sender or receiver),
     * ordered newest-first.
     */
    @Query(
            """
                    SELECT t FROM Transaction t
                    WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId)
                      AND t.status = 'COMPLETED'
                    ORDER BY t.createdAt DESC
                    """
    )
    Page<Transaction> findByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    /**
     * Sum of money that came IN to the account this month:
     * - DEPOSIT where toAccountId matches
     * - TRANSFER where toAccountId matches
     */
    @Query(
            """
                    SELECT COALESCE(SUM(t.amount), 0)
                    FROM Transaction t
                    WHERE t.toAccountId = :accountId
                      AND t.status = 'COMPLETED'
                      AND t.createdAt BETWEEN :start AND :end
                    """
    )
    BigDecimal sumCreditsByAccountId(
            @Param("accountId") Long accountId,
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end
                                    );

    /**
     * Sum of money that went OUT of the account this month:
     * - WITHDRAWAL where fromAccountId matches
     * - TRANSFER where fromAccountId matches
     */
    @Query(
            """
                    SELECT COALESCE(SUM(t.amount), 0)
                    FROM Transaction t
                    WHERE t.fromAccountId = :accountId
                      AND t.status = 'COMPLETED'
                      AND t.createdAt BETWEEN :start AND :end
                    """
    )
    BigDecimal sumDebitsByAccountId(
            @Param("accountId") Long accountId,
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end
                                   );
}