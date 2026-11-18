package com.fraud.gateway.config;

import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenApiCustomizer relativeServerCustomizer() {
    return openApi -> openApi.setServers(List.of(new Server().url("/")));
  }
}
