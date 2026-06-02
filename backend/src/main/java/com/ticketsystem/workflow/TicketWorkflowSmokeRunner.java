package com.ticketsystem.workflow;

import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * jBPM Faz 14a — build spike doğrulama komponenti.
 *
 * <p>Uygulama başlangıcında çalışır ve şu soruları yanıtlar:</p>
 * <ul>
 *   <li>KieContainer oluşturuldu mu?</li>
 *   <li>ticket-lifecycle BPMN process definition yüklendi mi?</li>
 *   <li>Bir ProcessInstance başlatılabiliyor mu?</li>
 * </ul>
 *
 * <p>Başarısız olursa uygulama çalışmaya devam eder —
 * mevcut ticket iş mantığı bu runner'dan bağımsızdır.</p>
 *
 * <p>Faz 14b onaylandığında bu sınıf TicketWorkflowService'e dönüştürülecek.</p>
 */
@Slf4j
@Component
public class TicketWorkflowSmokeRunner implements ApplicationRunner {

    private final KieContainer kieContainer;

    public TicketWorkflowSmokeRunner(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("jBPM smoke test başlıyor: ticket-lifecycle process doğrulanıyor...");
        KieSession session = null;
        try {
            session = kieContainer.newKieSession();
            ProcessInstance pi = session.startProcess("ticket-lifecycle");
            log.info("jBPM smoke test BAŞARILI — processId={} state={}",
                pi.getId(), pi.getState());
            log.info("jBPM Faz 14a spike GEÇTİ: "
                + "KieContainer + BPMN load + ProcessInstance oluşturma doğrulandı.");
        } catch (Exception e) {
            log.error("jBPM smoke test BAŞARISIZ — Faz 14a spike KALDI: {}",
                e.getMessage(), e);
            // Ticket iş mantığı etkilenmez; yalnızca spike sonucu loglanır
        } finally {
            if (session != null) {
                session.dispose();
            }
        }
    }
}
