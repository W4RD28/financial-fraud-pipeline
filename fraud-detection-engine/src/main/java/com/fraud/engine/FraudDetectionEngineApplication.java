package com.fraud.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@EnableKafkaStreams
public class FraudDetectionEngineApplication {

  public static void main(String[] args) {
    SpringApplication.run(FraudDetectionEngineApplication.class, args);
  }
}
