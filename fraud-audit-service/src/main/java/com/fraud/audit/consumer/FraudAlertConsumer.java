package com.fraud.audit.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class FraudAlertConsumer {

  @KafkaListener(
      topics = "${app.kafka.topics.fraud-alerts:fraud-alerts}",
      groupId = "${spring.kafka.consumer.group-id:fraud-audit-group}")
  public void onFraudAlert(ConsumerRecord<String, String> record, Acknowledgment ack) {
    try {
      System.out.println("Audit Service Received Fraud Alert: " + record.value());

      ack.acknowledge();
    } catch (Exception e) {
      throw e;
    }
  }
}
