package com.ticketsystem.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TicketEventProducer {

    private static final Logger log = LoggerFactory.getLogger(TicketEventProducer.class);
    private static final String TOPIC = "ticket-events";

    private final KafkaTemplate<String, TicketEvent> kafkaTemplate;

    public TicketEventProducer(KafkaTemplate<String, TicketEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(TicketEvent event) {
        try {
            kafkaTemplate.send(TOPIC, String.valueOf(event.ticketId()), event)
                    .exceptionally(ex -> {
                        log.warn("Kafka event gönderilemedi (async) — ticket işlemi etkilenmedi. " +
                                 "ticketId={}, action={}: {}",
                                 event.ticketId(), event.action(), ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            log.warn("Kafka event gönderilemedi (sync) — ticket işlemi etkilenmedi. " +
                     "ticketId={}, action={}: {}",
                     event.ticketId(), event.action(), e.getMessage());
        }
    }
}
