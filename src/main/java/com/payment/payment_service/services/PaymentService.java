package com.payment.payment_service.services;

import com.payment.payment_service.dto.PaymentRequest;
import com.payment.payment_service.dto.PaymentResponse;
import com.payment.payment_service.kafka.PaymentEvent;
import com.payment.payment_service.kafka.PaymentEventProducer;
import com.payment.payment_service.model.*;
import com.payment.payment_service.rateLimiter.RateLimitExceededException;
import com.payment.payment_service.rateLimiter.RateLimiterService;
import com.payment.payment_service.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final PaymentEventProducer eventProducer;
    private final RateLimiterService rateLimiterService;

    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request, String idempotencyKey) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!rateLimiterService.isAllowed(username)) {
            throw new RateLimitExceededException(
                    "Too many payment requests. Maximum " + 10 + " requests per minute allowed."
            );
        }

        // 1. Check idempotency — prevent duplicate payments
        if (idempotencyKey != null) {
            var existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Duplicate request detected for idempotency key: {}", idempotencyKey);
                return mapToResponse(existing.get(), "Duplicate request - returning existing transaction");
            }
        }

        // 2. Get the logged-in user from JWT context
//        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // 3. Validate merchant exists and is active
        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new RuntimeException("Merchant not found: " + request.getMerchantId()));

        if (!merchant.isActive()) {
            throw new RuntimeException("Merchant is not active");
        }

        // 4. Create transaction
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setMerchant(merchant);
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setStatus(TransactionStatus.INITIATED);
        transaction.setIdempotencyKey(idempotencyKey);

        transaction = transactionRepository.save(transaction);
        transaction = transactionRepository.save(transaction);

        log.info("Payment initiated: transactionId={}, amount={} {}",
                transaction.getId(), transaction.getAmount(), transaction.getCurrency());
        // Publish to Kafka
        PaymentEvent event = new PaymentEvent(
                transaction.getId(),
                user.getUsername(),
                transaction.getAmount(),
                transaction.getCurrency(),
                merchant.getName(),
                merchant.getCountry()
        );
        eventProducer.publishPaymentInitiated(event);

        return mapToResponse(transaction, "Payment initiated successfully");
    }

    public PaymentResponse getPayment(Long transactionId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));

        // Security check — users can only see their own transactions
        if (!transaction.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Access denied");
        }

        return mapToResponse(transaction, null);
    }

    public List<PaymentResponse> getPaymentHistory() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return transactionRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(t -> mapToResponse(t, null))
                .collect(Collectors.toList());
    }

    private PaymentResponse mapToResponse(Transaction t, String message) {
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(t.getId());
        response.setAmount(t.getAmount());
        response.setCurrency(t.getCurrency());
        response.setStatus(t.getStatus());
        response.setMerchantName(t.getMerchant().getName());
        response.setCreatedAt(t.getCreatedAt());
        response.setMessage(message);
        return response;
    }


}