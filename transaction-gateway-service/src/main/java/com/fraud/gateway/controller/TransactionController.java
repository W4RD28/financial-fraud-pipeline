package com.fraud.gateway.controller;

import com.fraud.common.dto.api.ApiErrorDetail;
import com.fraud.common.dto.api.ApiResponse;
import com.fraud.common.dto.api.PageMeta;
import com.fraud.gateway.dto.CreateTransactionRequest;
import com.fraud.gateway.dto.TransactionResponse;
import com.fraud.gateway.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(
    name = "Transaction Ingestion",
    description = "Ingest financial transactions into the transactional outbox and query history")
public class TransactionController {

  private final TransactionService transactionService;

  @PostMapping
  @Operation(
      summary = "Ingest a financial transaction",
      description =
          "Persists the transaction and stages an outbox event for asynchronous fraud evaluation.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "201",
        description = "Transaction persisted and staged in transactional outbox"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "Validation error in transaction request payload",
        content = @Content(schema = @Schema(implementation = ApiErrorDetail.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "409",
        description = "Duplicate transactionId already exists",
        content = @Content(schema = @Schema(implementation = ApiErrorDetail.class)))
  })
  public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
      @Valid @RequestBody CreateTransactionRequest request) {
    TransactionResponse response = transactionService.ingestTransaction(request);
    return ResponseEntity.created(URI.create("/api/v1/transactions/" + response.transactionId()))
        .body(ApiResponse.ok("Transaction ingested successfully", response));
  }

  @GetMapping("/{transactionId}")
  @Operation(
      summary = "Get a transaction by ID",
      description = "Fetches a single persisted transaction record by its unique transactionId.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Transaction found"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "Transaction does not exist",
        content = @Content(schema = @Schema(implementation = ApiErrorDetail.class)))
  })
  public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(
      @Parameter(
              description = "Unique transaction identifier",
              example = "ff5acd02-0d98-483b-928e-f0cfd86af69b")
          @PathVariable
          String transactionId) {
    return ResponseEntity.ok(ApiResponse.ok(transactionService.getById(transactionId)));
  }

  @GetMapping
  @Operation(
      summary = "List ingested transactions with optional account filter and pagination",
      description =
          "Returns a paginated list of ingested transactions sorted by occurrence time, optionally filtered by accountId.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Paginated transactions retrieved successfully"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "Invalid pagination or query parameter",
        content = @Content(schema = @Schema(implementation = ApiErrorDetail.class)))
  })
  public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions(
      @Parameter(description = "Optional filter by customer account ID", example = "ACC-1001")
          @RequestParam(required = false)
          String accountId,
      @ParameterObject
          @PageableDefault(size = 20, sort = "occurredAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    Page<TransactionResponse> page = transactionService.listTransactions(accountId, pageable);
    PageMeta meta =
        PageMeta.ofZeroIndexed(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext(),
            page.hasPrevious());
    return ResponseEntity.ok(ApiResponse.paged(page.getContent(), meta));
  }
}
