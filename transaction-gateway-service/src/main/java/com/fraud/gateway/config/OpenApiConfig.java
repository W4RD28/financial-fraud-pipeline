package com.fraud.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI transactionGatewayOpenApi() {
    return new OpenAPI()
        .servers(List.of(new Server().url("/")))
        .info(
            new Info()
                .title("Transaction Gateway Service API")
                .version("1.0.0")
                .description(
                    "Ingests financial transactions and writes to the transactional outbox."));
  }
}
