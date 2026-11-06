package com.fraud.engine.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("!prod")
public class KafkaTopicConfig {

    @Value("${app.kafka.topics.raw-transactions:raw-transactions}")
    private String rawTransactionsTopic;

    @Value("${app.kafka.topics.fraud-alerts:fraud-alerts}")
    private String fraudAlertsTopic;

    @Bean
    public NewTopic rawTransactionsTopic() {
        return TopicBuilder.name(rawTransactionsTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic fraudAlertsTopic() {
        return TopicBuilder.name(fraudAlertsTopic).partitions(3).replicas(1).build();
    }
}
