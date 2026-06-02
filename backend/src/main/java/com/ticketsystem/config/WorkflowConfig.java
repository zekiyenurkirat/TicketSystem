package com.ticketsystem.config;

import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.internal.io.ResourceFactory;
import org.kie.api.runtime.KieContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * jBPM KIE altyapı konfigürasyonu.
 *
 * <p>Spring Boot starter kullanılmamaktadır (yalnızca SB 2.x destekler).
 * KieServices / KieFileSystem / KieBuilder / KieContainer manuel olarak
 * kurulur ve BPMN process definition classpath'ten yüklenir.</p>
 *
 * <p>Faz 14a — spike: BPMN build ve KieContainer oluşturma doğrulaması.</p>
 */
@Slf4j
@Configuration
public class WorkflowConfig {

    @Bean
    public KieContainer kieContainer() {
        log.info("jBPM: KieContainer başlatılıyor...");

        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();

        // ticket-lifecycle.bpmn2 classpath'ten yükleniyor
        kfs.write(ResourceFactory.newClassPathResource("processes/ticket-lifecycle.bpmn2"));

        KieBuilder kb = ks.newKieBuilder(kfs);
        kb.buildAll();

        // Build hataları uygulamayı durdurur — spike sonucu net olsun
        if (kb.getResults().hasMessages(Message.Level.ERROR)) {
            throw new IllegalStateException(
                "jBPM BPMN build hatası (Faz 14a spike başarısız): "
                + kb.getResults().getMessages()
            );
        }

        KieContainer container = ks.newKieContainer(
            ks.getRepository().getDefaultReleaseId()
        );
        log.info("jBPM: KieContainer başarıyla yüklendi.");
        return container;
    }
}
