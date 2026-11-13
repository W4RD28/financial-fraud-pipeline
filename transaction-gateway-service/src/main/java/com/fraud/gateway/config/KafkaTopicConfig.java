package com.fraud.gateway.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
@Profile("!prod")
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${app.kafka.topics.raw-transactions:raw-transactions}")
    private String rawTransactionsTopic;

    @Value("${app.kafka.topics.fraud-alerts:fraud-alerts}")
    private String fraudAlertsTopic;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic rawTransactionsTopic() {
        return TopicBuilder.name(rawTransactionsTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic fraudAlertsTopic() {
        return TopicBuilder.name(fraudAlertsTopic).partitions(3).replicas(1).build();
    }
}
