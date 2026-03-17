package com.payment.payment_service.kafka;

import com.payment.payment_service.model.Transaction;
import com.payment.payment_service.model.TransactionStatus;
import com.payment.payment_service.repositories.TransactionRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class FraudWorker {

    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "payment_initiated", groupId = "fraud-group")
    public void processPayment(PaymentEvent event) {
        log.info("FraudWorker received payment: transactionId={}", event.getTransactionId());

        // Run fraud rules
        FraudResult result = evaluateFraud(event);

        // Update transaction status
        transactionRepository.findById(event.getTransactionId()).ifPresent(transaction -> {
            if (result.isApproved()) {
                transaction.setStatus(TransactionStatus.APPROVED);
                log.info("Transaction {} APPROVED (score={})", transaction.getId(), result.getScore());
            } else {
                transaction.setStatus(TransactionStatus.DECLINED);
                log.warn("Transaction {} DECLINED - reason: {} (score={})",
                        transaction.getId(), result.getReason(), result.getScore());
            }
            transactionRepository.save(transaction);
        });

        // Publish result event
        FraudResultEvent resultEvent = new FraudResultEvent(
                event.getTransactionId(),
                result.isApproved(),
                result.getReason(),
                result.getScore()
        );

        String topic = result.isApproved() ? "payment_approved" : "payment_rejected";
        kafkaTemplate.send(topic, String.valueOf(event.getTransactionId()), resultEvent);
    }

    private FraudResult evaluateFraud(PaymentEvent event) {
        int score = 0;
        String reason = null;

        // Rule 1: High amount
        if (event.getAmount().compareTo(new BigDecimal("50000")) > 0) {
            score += 80;
            reason = "Amount exceeds threshold";
        }

        // Rule 2: Suspicious currency
        if ("ZWL".equals(event.getCurrency())) {
            score += 30;
            reason = "Suspicious currency";
        }

        // Rule 3: High risk country
        if ("KP".equals(event.getMerchantCountry())) {
            score += 80;
            reason = "High risk merchant country";
        }

        boolean approved = score < 70;
        if (approved) reason = "Passed fraud checks";

        return new FraudResult(approved, reason, score);
    }

    // Simple internal result holder
    @Data
    @AllArgsConstructor
    private static class FraudResult {
        private boolean approved;
        private String reason;
        private int score;
    }
}