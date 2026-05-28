package com.ticketsystem.dto.response;

import com.ticketsystem.entity.SlaRule;
import com.ticketsystem.entity.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** SLA kural bilgilerini dışarıya taşıyan yanıt nesnesi. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SlaRuleResponse {

    private Long id;
    private Priority priority;
    private Integer responseTimeHours;
    private Integer resolutionTimeHours;
    private Integer escalationTimeHours;
    private boolean active;

    /** {@link SlaRule} entity'sinden {@link SlaRuleResponse} üretir. */
    public static SlaRuleResponse from(SlaRule rule) {
        return new SlaRuleResponse(
                rule.getId(),
                rule.getPriority(),
                rule.getResponseTimeHours(),
                rule.getResolutionTimeHours(),
                rule.getEscalationTimeHours(),
                rule.isActive()
        );
    }
}
