package com.payment.payment_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic paymentInitiatedTopic() {
        return TopicBuilder.name("payment_initiated").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic paymentApprovedTopic() {
        return TopicBuilder.name("payment_approved").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic paymentRejectedTopic() {
        return TopicBuilder.name("payment_rejected").partitions(1).replicas(1).build();
    }
}