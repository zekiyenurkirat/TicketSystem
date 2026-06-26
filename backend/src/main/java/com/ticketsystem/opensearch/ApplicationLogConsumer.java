package com.ticketsystem.opensearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.UUID;

/**
 * application-logs Kafka topic'ini dinler; her log satırını OpenSearch'e yazar.
 *
 * Bu sınıf com.ticketsystem.opensearch paketindedir. log4j2.xml'de bu paket için
 * KafkaAppender'sız ayrı bir logger tanımlanmıştır — recursive logging riski sıfırdır.
 */
@Component
public class ApplicationLogConsumer {

    private static final Logger log = LoggerFactory.getLogger(ApplicationLogConsumer.class);
    private static final String INDEX = "application-logs";

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ApplicationLogConsumer(
            RestTemplate restTemplate,
            @Value("${app.opensearch.host:localhost}") String host,
            @Value("${app.opensearch.port:9200}") int port) {
        this.restTemplate = restTemplate;
        this.baseUrl = "http://" + host + ":" + port;
    }

    @KafkaListener(
            topics = "application-logs",
            groupId = "application-logs-consumer",
            containerFactory = "stringKafkaListenerContainerFactory"
    )
    public void consume(String logMessage) {
        String documentId = UUID.randomUUID().toString();
        String url = baseUrl + "/" + INDEX + "/_doc/" + documentId;
        try {
            String json = buildDocument(logMessage);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(json, headers), String.class);
        } catch (Exception e) {
            // Soft-fail: OpenSearch erişilemese bile uygulama çalışmaya devam eder.
            // Bu warn logu com.ticketsystem.opensearch logger'ı üzerinden gider → KafkaAppender'a ulaşmaz.
            log.warn("application-logs OpenSearch indexleme başarısız — soft-fail. documentId={}: {}",
                    documentId, e.getMessage());
        }
    }

    private String buildDocument(String message) {
        String escapedMessage = message
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
        return "{\"message\":\"" + escapedMessage + "\",\"ingestedAt\":\"" + Instant.now() + "\"}";
    }
}
