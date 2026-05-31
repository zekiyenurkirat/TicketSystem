package com.ticketsystem.dto.request;

import com.ticketsystem.entity.enums.Priority;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** AGENT/MANAGER tarafından ticket priority review isteği için veri taşıyıcı. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PriorityReviewRequest {

    /** Yeni aktif/final öncelik. Tüm Priority değerleri geçerlidir. */
    @NotNull
    private Priority priority;

    /** Priority review için opsiyonel triage notu. */
    @Size(max = 1000)
    private String reviewNote;
}
