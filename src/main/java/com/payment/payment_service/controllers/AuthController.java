package com.payment.payment_service.controllers;


import com.payment.payment_service.dto.LoginRequest;
import com.payment.payment_service.dto.LoginResponse;
import com.payment.payment_service.model.Role;
import com.payment.payment_service.repositories.MerchantRepository;
import com.payment.payment_service.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MerchantRepository merchantRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // Temporary endpoint to seed a test user — remove before production
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String username,
                                           @RequestParam String password,
                                           @RequestParam Role role) {
        authService.register(username, password, role);
        return ResponseEntity.ok("User created");
    }

    @PostMapping("/seed-merchant")
    public ResponseEntity<String> seedMerchant() {
        com.payment.payment_service.model.Merchant merchant =
                new com.payment.payment_service.model.Merchant();
        merchant.setName("Amazon");
        merchant.setCountry("US");
        merchantRepository.save(merchant);
        return ResponseEntity.ok("Merchant created");
    }
}

