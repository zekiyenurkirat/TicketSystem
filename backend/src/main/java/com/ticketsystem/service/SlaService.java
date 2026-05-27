package com.ticketsystem.service;

import com.ticketsystem.entity.SlaRule;
import com.ticketsystem.entity.enums.Priority;

import java.time.LocalDateTime;

/** SLA kurallarını sorgulama ve bitiş tarihi hesaplama işlemlerini tanımlar. */
public interface SlaService {

    /** Verilen öncelik için aktif SLA kuralını döner. */
    SlaRule findActiveSlaRule(Priority priority);

    /** Verilen önceliğe göre SLA bitiş tarihini hesaplar. */
    LocalDateTime calculateDueDate(Priority priority);
}
