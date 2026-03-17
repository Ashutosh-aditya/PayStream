package com.payment.payment_service.repositories;

import com.payment.payment_service.model.Transaction;
import com.payment.payment_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    List<Transaction> findByUserOrderByCreatedAtDesc(User user);
}