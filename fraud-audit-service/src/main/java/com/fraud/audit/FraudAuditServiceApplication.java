package com.fraud.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FraudAuditServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(FraudAuditServiceApplication.class, args);
  }
}
