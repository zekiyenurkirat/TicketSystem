package com.ticketsystem.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.Map;

/**
 * application-logs topic'i için String tabanlı Kafka consumer factory.
 * Mevcut ticket-events pipeline'ı JsonDeserializer + TicketEvent tipini kullanır;
 * bu factory o konfigürasyona dokunmadan String mesaj tüketebilir.
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    // Default auto-config factory'siyle tutarlı olmak için aynı property okunur.
    // application-test.yaml: auto-startup=false → Kafka NetworkClient test sırasında açılmaz.
    @Value("${spring.kafka.listener.auto-startup:true}")
    private boolean autoStartup;

    @Bean
    public ConsumerFactory<String, String> stringConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        ));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> stringKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stringConsumerFactory());
        factory.setAutoStartup(autoStartup);
        return factory;
    }
}
