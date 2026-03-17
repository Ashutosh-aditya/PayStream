package com.payment.payment_service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationWorker {

    @KafkaListener(topics = "payment_approved", groupId = "notification-group")
    public void onPaymentApproved(FraudResultEvent event) {
        log.info("NOTIFICATION: Payment {} approved! Sending confirmation to user.",
                event.getTransactionId());
        // In real life: send email/SMS here
    }

    @KafkaListener(topics = "payment_rejected", groupId = "notification-group")
    public void onPaymentRejected(FraudResultEvent event) {
        log.warn("NOTIFICATION: Payment {} rejected! Reason: {}",
                event.getTransactionId(), event.getReason());
        // In real life: send alert email here
    }
}