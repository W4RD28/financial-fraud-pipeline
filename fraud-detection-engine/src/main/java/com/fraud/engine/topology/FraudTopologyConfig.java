package com.fraud.engine.topology;

import com.fraud.common.dto.event.FraudAlertEvent;
import com.fraud.common.dto.event.TransactionDecisionEvent;
import com.fraud.common.dto.event.TransactionDecisionStatus;
import com.fraud.common.dto.event.TransactionEvent;
import com.fraud.engine.model.AccountWindowStats;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerde;

@Configuration
@Slf4j
public class FraudTopologyConfig {

  @Value("${app.kafka.topics.raw-transactions:raw-transactions}")
  private String rawTransactionsTopic;

  @Value("${app.kafka.topics.fraud-alerts:fraud-alerts}")
  private String fraudAlertsTopic;

  @Value("${app.kafka.topics.transaction-decisions:transaction-decisions}")
  private String transactionDecisionsTopic;

  @Value("${app.fraud.velocity-window-seconds:60}")
  private long windowSeconds;

  @Value("${app.fraud.max-transactions-per-window:3}")
  private long maxTransactionsPerWindow;

  @Value("${app.fraud.high-value-threshold:5000.00}")
  private BigDecimal highValueThreshold;

  private <T> JacksonJsonSerde<T> jsonSerde(Class<T> targetType) {
    JacksonJsonSerde<T> serde = new JacksonJsonSerde<>(targetType);
    serde.configure(
        Map.of(
            JacksonJsonDeserializer.TRUSTED_PACKAGES,
            "com.fraud.common.dto,com.fraud.engine.model"),
        false);
    return serde;
  }

  @Bean
  public KStream<String, TransactionEvent> fraudDetectionTopology(StreamsBuilder builder) {
    JacksonJsonSerde<TransactionEvent> transactionSerde = jsonSerde(TransactionEvent.class);
    JacksonJsonSerde<AccountWindowStats> statsSerde = jsonSerde(AccountWindowStats.class);
    JacksonJsonSerde<FraudAlertEvent> alertSerde = jsonSerde(FraudAlertEvent.class);
    JacksonJsonSerde<TransactionDecisionEvent> decisionSerde =
        jsonSerde(TransactionDecisionEvent.class);

    KStream<String, TransactionEvent> transactions =
        builder.stream(rawTransactionsTopic, Consumed.with(Serdes.String(), transactionSerde));

    KStream<Windowed<String>, AccountWindowStats> aggregatedStream =
        transactions
            .groupByKey(Grouped.with(Serdes.String(), transactionSerde))
            .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(windowSeconds)))
            .aggregate(
                AccountWindowStats::empty,
                (accountId, event, aggregate) -> aggregate.add(event),
                Materialized.<String, AccountWindowStats, WindowStore<Bytes, byte[]>>as(
                        "account-velocity-store")
                    .withKeySerde(Serdes.String())
                    .withValueSerde(statsSerde))
            .toStream();

    // Branch 1: Emit FraudAlertEvent when velocity or amount thresholds are
    // breached
    aggregatedStream
        .filter((windowedKey, stats) -> isSuspicious(stats))
        .map(
            (windowedKey, stats) -> {
              String accountId = windowedKey.key();
              FraudAlertEvent alert = buildAlert(accountId, stats);
              log.warn(
                  "Fraud alert triggered: alertId={} accountId={} rule={} count={} amount={}",
                  alert.alertId(),
                  accountId,
                  alert.ruleTriggered(),
                  alert.velocityCount(),
                  alert.amount());
              return KeyValue.pair(accountId, alert);
            })
        .to(fraudAlertsTopic, Produced.with(Serdes.String(), alertSerde));

    // Branch 2: Emit TransactionDecisionEvent for every evaluated transaction
    aggregatedStream
        .filter((windowedKey, stats) -> stats.latestTransactionId() != null)
        .map(
            (windowedKey, stats) -> {
              String accountId = windowedKey.key();
              TransactionDecisionEvent decision = buildDecision(accountId, stats);
              log.info(
                  "Decision emitted: transactionId={} accountId={} decision={} score={} rule={}",
                  decision.transactionId(),
                  accountId,
                  decision.decision(),
                  decision.riskScore(),
                  decision.triggeredRule());
              return KeyValue.pair(decision.transactionId(), decision);
            })
        .to(transactionDecisionsTopic, Produced.with(Serdes.String(), decisionSerde));

    return transactions;
  }

  private boolean isSuspicious(AccountWindowStats stats) {
    boolean velocityBreached = stats.transactionCount() > maxTransactionsPerWindow;
    boolean amountBreached = stats.totalAmount().compareTo(highValueThreshold) >= 0;
    return velocityBreached || amountBreached;
  }

  private FraudAlertEvent buildAlert(String accountId, AccountWindowStats stats) {
    String rule = resolveTriggeredRule(stats);

    return new FraudAlertEvent(
        UUID.randomUUID().toString(),
        stats.latestTransactionId(),
        accountId,
        stats.totalAmount(),
        rule,
        Math.toIntExact(stats.transactionCount()),
        Instant.now());
  }

  private TransactionDecisionEvent buildDecision(String accountId, AccountWindowStats stats) {
    boolean velocityBreached = stats.transactionCount() > maxTransactionsPerWindow;
    boolean amountBreached = stats.totalAmount().compareTo(highValueThreshold) >= 0;

    if (velocityBreached && amountBreached) {
      return new TransactionDecisionEvent(
          stats.latestTransactionId(),
          accountId,
          TransactionDecisionStatus.DECLINED,
          0.95,
          "VELOCITY_AND_HIGH_VALUE_EXCEEDED",
          String.format(
              "Count %d exceeded limit %d and window total %s exceeded %s",
              stats.transactionCount(),
              maxTransactionsPerWindow,
              stats.totalAmount(),
              highValueThreshold),
          "FRAUD_ENGINE",
          Instant.now());
    }

    if (amountBreached) {
      return new TransactionDecisionEvent(
          stats.latestTransactionId(),
          accountId,
          TransactionDecisionStatus.DECLINED,
          0.85,
          "HIGH_VALUE_WINDOW_EXCEEDED",
          String.format(
              "Window total %s exceeded threshold %s", stats.totalAmount(), highValueThreshold),
          "FRAUD_ENGINE",
          Instant.now());
    }

    if (velocityBreached) {
      return new TransactionDecisionEvent(
          stats.latestTransactionId(),
          accountId,
          TransactionDecisionStatus.HELD_FOR_REVIEW,
          0.65,
          "VELOCITY_LIMIT_EXCEEDED",
          String.format(
              "Transaction count %d exceeded limit %d within %ds window",
              stats.transactionCount(), maxTransactionsPerWindow, windowSeconds),
          "FRAUD_ENGINE",
          Instant.now());
    }

    double baselineScore =
        stats
            .totalAmount()
            .divide(highValueThreshold, 4, RoundingMode.HALF_UP)
            .min(BigDecimal.valueOf(0.49))
            .doubleValue();

    return new TransactionDecisionEvent(
        stats.latestTransactionId(),
        accountId,
        TransactionDecisionStatus.APPROVED,
        baselineScore,
        "NONE",
        "Passed velocity and high-value window checks",
        "FRAUD_ENGINE",
        Instant.now());
  }

  private String resolveTriggeredRule(AccountWindowStats stats) {
    return stats.transactionCount() > maxTransactionsPerWindow
        ? "VELOCITY_LIMIT_EXCEEDED"
        : "HIGH_VALUE_WINDOW_EXCEEDED";
  }
}
