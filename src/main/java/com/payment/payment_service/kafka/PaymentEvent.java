package com.payment.payment_service.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    private Long transactionId;
    private String username;
    private BigDecimal amount;
    private String currency;
    private String merchantName;
    private String merchantCountry;
}
