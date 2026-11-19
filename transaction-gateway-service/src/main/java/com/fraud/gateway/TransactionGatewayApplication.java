package com.fraud.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TransactionGatewayApplication {

  public static void main(String[] args) {
    SpringApplication.run(TransactionGatewayApplication.class, args);
  }
}
