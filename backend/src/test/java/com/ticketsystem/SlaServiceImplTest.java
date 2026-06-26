package com.ticketsystem;

import com.ticketsystem.core.exception.ResourceNotFoundException;
import com.ticketsystem.entity.SlaRule;
import com.ticketsystem.entity.enums.Priority;
import com.ticketsystem.repository.SlaRuleRepository;
import com.ticketsystem.service.impl.SlaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlaServiceImplTest {

    @Mock
    private SlaRuleRepository slaRuleRepository;

    @InjectMocks
    private SlaServiceImpl slaService;

    private SlaRule highRule;

    @BeforeEach
    void setUp() {
        highRule = new SlaRule();
        highRule.setId(1L);
        highRule.setPriority(Priority.HIGH);
        highRule.setResolutionTimeHours(4);
        highRule.setActive(true);
    }

    @Test
    void findActiveSlaRule_returnsRuleWhenFound() {
        when(slaRuleRepository.findByPriorityAndActiveTrue(Priority.HIGH))
                .thenReturn(Optional.of(highRule));

        SlaRule result = slaService.findActiveSlaRule(Priority.HIGH);

        assertThat(result).isNotNull();
        assertThat(result.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(result.getResolutionTimeHours()).isEqualTo(4);
    }

    @Test
    void findActiveSlaRule_throwsWhenNotFound() {
        when(slaRuleRepository.findByPriorityAndActiveTrue(Priority.LOW))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> slaService.findActiveSlaRule(Priority.LOW))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("LOW");
    }

    @Test
    void calculateDueDate_returnsNowPlusResolutionHours() {
        when(slaRuleRepository.findByPriorityAndActiveTrue(Priority.HIGH))
                .thenReturn(Optional.of(highRule));

        LocalDateTime before = LocalDateTime.now();
        LocalDateTime dueDate = slaService.calculateDueDate(Priority.HIGH);
        LocalDateTime after = LocalDateTime.now();

        assertThat(dueDate).isAfterOrEqualTo(before.plusHours(4));
        assertThat(dueDate).isBeforeOrEqualTo(after.plusHours(4));
    }

    @Test
    void calculateDueDate_queriesRepositoryOnce() {
        when(slaRuleRepository.findByPriorityAndActiveTrue(Priority.HIGH))
                .thenReturn(Optional.of(highRule));

        slaService.calculateDueDate(Priority.HIGH);

        verify(slaRuleRepository, times(1)).findByPriorityAndActiveTrue(Priority.HIGH);
    }

    @Test
    void findActiveSlaRule_differentPriorities_callRepositoryWithCorrectParam() {
        SlaRule medRule = new SlaRule();
        medRule.setPriority(Priority.MEDIUM);
        medRule.setResolutionTimeHours(8);
        medRule.setActive(true);

        when(slaRuleRepository.findByPriorityAndActiveTrue(Priority.MEDIUM))
                .thenReturn(Optional.of(medRule));

        SlaRule result = slaService.findActiveSlaRule(Priority.MEDIUM);

        assertThat(result.getPriority()).isEqualTo(Priority.MEDIUM);
        verify(slaRuleRepository).findByPriorityAndActiveTrue(Priority.MEDIUM);
        verifyNoMoreInteractions(slaRuleRepository);
    }
}
