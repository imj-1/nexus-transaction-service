package com.nexus.banking.transaction_service.common.repository;

import com.nexus.banking.transaction_service.common.entity.Transaction;
import com.nexus.banking.transaction_service.common.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionReference(String transactionReference);

    /**
     * All transactions involving the given account as sender OR receiver.
     */
    @Query(
            """
                    SELECT t FROM Transaction t
                    WHERE t.fromAccountId = :accountId
                       OR t.toAccountId   = :accountId
                    ORDER BY t.createdAt DESC
                    """
    )
    Page<Transaction> findByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    Page<Transaction> findByInitiatedByOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<Transaction> findByStatus(TransactionStatus status);
}