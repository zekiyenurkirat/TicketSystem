package com.ticketsystem.workflow;

import com.ticketsystem.entity.enums.TicketStatus;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * jBPM ticket yaşam döngüsü iş akışı servisi.
 *
 * <p>Ticket oluşturulunca process başlatır; status/atama değişikliklerinde
 * ilgili process instance'a sinyal gönderir.</p>
 *
 * <p><strong>Soft-fail garantileri:</strong>
 * <ul>
 *   <li>{@code processInstanceId} null/boş → debug log, sinyal gönderilmez</li>
 *   <li>{@code processInstanceId} parse edilemez → warn log, sinyal gönderilmez</li>
 *   <li>jBPM exception → warn log, exception yutulur, ticket işlemi devam eder</li>
 *   <li>Process o anda ilgili sinyali beklemiyor → jBPM sessizce yok sayar</li>
 * </ul>
 * </p>
 *
 * <p><strong>Bilinen sınırlama:</strong> In-memory KieSession uygulama yeniden
 * başlatılınca process state'i kaybeder. processInstanceId DB'de kalır ama
 * restart sonrası jBPM içinde eşleşen process yoktur.</p>
 *
 * <p>KieSession thread-safe olmadığından tüm public metodlar
 * {@code synchronized} ile korunur.</p>
 */
@Slf4j
@Service
public class TicketWorkflowService {

    private final KieSession kieSession;

    public TicketWorkflowService(KieContainer kieContainer) {
        this.kieSession = kieContainer.newKieSession();
        log.info("jBPM: TicketWorkflowService KieSession oluşturuldu.");
    }

    /**
     * Ticket oluşturulunca {@code ticket-lifecycle} process başlatır.
     *
     * @param ticketId     oluşturulan ticket'ın DB ID'si
     * @param ticketNumber oluşturulan ticket'ın numarası (örn. TK-XXXXXXXX)
     * @return processInstanceId string olarak; başarısız olursa {@code null}
     */
    public synchronized String startProcess(Long ticketId, String ticketNumber) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("ticketId", ticketId);
            params.put("ticketNumber", ticketNumber);
            ProcessInstance pi = kieSession.startProcess("ticket-lifecycle", params);
            String pid = String.valueOf(pi.getId());
            log.info("jBPM: Süreç başlatıldı. ticketId={} processInstanceId={}", ticketId, pid);
            return pid;
        } catch (Exception e) {
            log.warn("jBPM: Süreç başlatılamadı (soft-fail). ticketId={} hata={}",
                ticketId, e.getMessage());
            return null;
        }
    }

    /**
     * Ticket status veya atama değişikliğinde ilgili process instance'a sinyal gönderir.
     *
     * @param processInstanceId Ticket entity'deki string processInstanceId
     * @param newStatus         Ticket'ın geçtiği yeni durum
     */
    public synchronized void signalProcess(String processInstanceId, TicketStatus newStatus) {
        if (processInstanceId == null || processInstanceId.isBlank()) {
            log.debug("jBPM: processInstanceId null/boş, sinyal atlanıyor. status={}", newStatus);
            return;
        }

        String signal = toSignalName(newStatus);
        if (signal == null) {
            log.debug("jBPM: {} için sinyal tanımı yok, atlanıyor.", newStatus);
            return;
        }

        long pid;
        try {
            pid = Long.parseLong(processInstanceId);
        } catch (NumberFormatException e) {
            log.warn("jBPM: processInstanceId parse edilemedi: '{}', sinyal atlanıyor.", processInstanceId);
            return;
        }

        try {
            kieSession.signalEvent(signal, null, pid);
            log.info("jBPM: Sinyal gönderildi. processInstanceId={} signal={}", pid, signal);
        } catch (Exception e) {
            log.warn("jBPM: Sinyal gönderilemedi (soft-fail). processInstanceId={} signal={} hata={}",
                pid, signal, e.getMessage());
        }
    }

    /** TicketStatus → jBPM sinyal adına dönüştürür. NEW için tanım yoktur. */
    private String toSignalName(TicketStatus status) {
        return switch (status) {
            case ASSIGNED             -> "ASSIGNED";
            case IN_PROGRESS          -> "IN_PROGRESS";
            case WAITING_FOR_CUSTOMER -> "WAITING_FOR_CUSTOMER";
            case RESOLVED             -> "RESOLVED";
            case CLOSED               -> "CLOSED";
            case CANCELLED            -> "CANCELLED";
            default                   -> null;
        };
    }
}
