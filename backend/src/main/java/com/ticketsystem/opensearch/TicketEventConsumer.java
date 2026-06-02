package com.ticketsystem.opensearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketsystem.kafka.TicketEvent;
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

@Component
public class TicketEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TicketEventConsumer.class);
    private static final String INDEX = "ticket-events";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public TicketEventConsumer(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${app.opensearch.host:localhost}") String host,
            @Value("${app.opensearch.port:9200}") int port) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.baseUrl = "http://" + host + ":" + port;
    }

    @KafkaListener(topics = "ticket-events", groupId = "ticket-events-consumer")
    public void consume(TicketEvent event) {
        // PUT /_doc/{id} — idempotent upsert; aynı event iki kez gelirse üzerine yazar
        String documentId = event.ticketId() + "-" + event.action() + "-" + event.timestamp();
        String url = baseUrl + "/" + INDEX + "/_doc/" + documentId;
        try {
            String json = objectMapper.writeValueAsString(event);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(json, headers), String.class);
            log.info("OpenSearch event indexlendi. ticketId={}, action={}",
                    event.ticketId(), event.action());
        } catch (Exception e) {
            log.warn("OpenSearch indexleme başarısız — soft-fail, ticket işlemi etkilenmedi. " +
                     "ticketId={}, action={}: {}",
                    event.ticketId(), event.action(), e.getMessage());
        }
    }
}
