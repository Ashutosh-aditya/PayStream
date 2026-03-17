package com.payment.payment_service.dto;

import com.payment.payment_service.model.TransactionStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long transactionId;
    private BigDecimal amount;
    private String currency;
    private TransactionStatus status;
    private String merchantName;
    private LocalDateTime createdAt;
    private String message;
}