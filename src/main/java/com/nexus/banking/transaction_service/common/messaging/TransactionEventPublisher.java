package com.nexus.banking.transaction_service.common.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.banking.transaction_service.common.entity.Transaction;
import com.nexus.banking.transaction_service.common.messaging.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventPublisher {

    private static final String TOPIC = "transaction.events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Publishes async — failure is logged but does not roll back the transaction.
     * The transactionReference is used as the Kafka message key for ordering
     * guarantees within a single transaction's lifecycle events.
     */
    public void publish(Transaction transaction) {
        try {
            TransactionEvent event = new TransactionEvent(
                    transaction.getTransactionReference(),
                    transaction.getFromAccountId(),
                    transaction.getToAccountId(),
                    transaction.getAmount(),
                    transaction.getType(),
                    transaction.getStatus(),
                    transaction.getInitiatedBy(),
                    transaction.getFailureReason(),
                    LocalDateTime.now()
            );
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, transaction.getTransactionReference(), payload)
                         .whenComplete((result, ex) -> {
                             if (ex != null) {
                                 log.error(
                                         "Failed to publish event ref={}: {}",
                                         transaction.getTransactionReference(),
                                         ex.getMessage()
                                          );
                             } else {
                                 log.debug(
                                         "Published event ref={} status={} offset={}",
                                         transaction.getTransactionReference(),
                                         transaction.getStatus(),
                                         result.getRecordMetadata()
                                               .offset()
                                          );
                             }
                         });
        } catch (Exception e) {
            log.error("Could not serialize event ref={}: {}", transaction.getTransactionReference(), e.getMessage(), e);
        }
    }
}
