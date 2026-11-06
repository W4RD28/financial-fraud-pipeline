package com.fraud.engine.topology;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VelocityCheckTopology {

    @Value("${app.kafka.topics.raw-transactions:raw-transactions}")
    private String inputTopic;

    @Value("${app.kafka.topics.fraud-alerts:fraud-alerts}")
    private String outputTopic;

    @Autowired
    public void buildPipeline(StreamsBuilder builder) {
      builder.stream(inputTopic, Consumed.with(Serdes.String(), Serdes.String()))
        // .peek((key, value) -> log.info("Processing event: {}", value))
        .to(outputTopic, Produced.with(Serdes.String(), Serdes.String()));    
    }
}
