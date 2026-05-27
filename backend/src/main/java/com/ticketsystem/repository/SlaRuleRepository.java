package com.ticketsystem.repository;

import com.ticketsystem.entity.SlaRule;
import com.ticketsystem.entity.enums.Priority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** SLA kuralı veritabanı erişim arayüzü. */
public interface SlaRuleRepository extends JpaRepository<SlaRule, Long> {

    /** Belirtilen öncelik için aktif olan SLA kuralını getirir. */
    Optional<SlaRule> findByPriorityAndActiveTrue(Priority priority);
}
