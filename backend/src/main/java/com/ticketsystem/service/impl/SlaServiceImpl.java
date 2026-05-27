package com.ticketsystem.service.impl;

import com.ticketsystem.entity.SlaRule;
import com.ticketsystem.entity.enums.Priority;
import com.ticketsystem.repository.SlaRuleRepository;
import com.ticketsystem.service.SlaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** {@link SlaService} arayüzünün varsayılan uygulaması. */
@Service
public class SlaServiceImpl implements SlaService {

    private final SlaRuleRepository slaRuleRepository;

    public SlaServiceImpl(SlaRuleRepository slaRuleRepository) {
        this.slaRuleRepository = slaRuleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SlaRule findActiveSlaRule(Priority priority) {
        return slaRuleRepository.findByPriorityAndActiveTrue(priority)
                .orElseThrow(() -> new RuntimeException(
                        "Aktif SLA kuralı bulunamadı: " + priority));
    }

    @Override
    @Transactional(readOnly = true)
    public LocalDateTime calculateDueDate(Priority priority) {
        SlaRule rule = findActiveSlaRule(priority);
        return LocalDateTime.now().plusHours(rule.getResolutionTimeHours());
    }
}
