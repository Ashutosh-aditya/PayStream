package com.payment.payment_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentInitiated(PaymentEvent event) {
        log.info("Publishing payment_initiated for transactionId={}", event.getTransactionId());
        kafkaTemplate.send("payment_initiated",
                String.valueOf(event.getTransactionId()),
                event);
    }
}