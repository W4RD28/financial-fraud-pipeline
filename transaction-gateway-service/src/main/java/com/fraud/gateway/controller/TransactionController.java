package com.fraud.gateway.controller;

import com.fraud.gateway.dto.CreateTransactionRequest;
import com.fraud.gateway.dto.TransactionResponse;
import com.fraud.gateway.service.TransactionService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse response = transactionService.ingestTransaction(request);
        return ResponseEntity.created(
                        URI.create("/api/v1/transactions/" + response.transactionId()))
                .body(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable String transactionId) {
        return ResponseEntity.ok(transactionService.getById(transactionId));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getTransactionsByAccount(
            @RequestParam String accountId) {
        return ResponseEntity.ok(transactionService.getByAccountId(accountId));
    }
}
