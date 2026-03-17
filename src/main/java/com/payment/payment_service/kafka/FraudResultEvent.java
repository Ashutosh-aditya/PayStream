package com.payment.payment_service.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudResultEvent {
    private Long transactionId;
    private boolean approved;
    private String reason;
    private int fraudScore;
}