package com.ticketsystem.service;

import com.ticketsystem.dto.request.CreateAssignmentRequestRequest;
import com.ticketsystem.dto.response.AssignmentRequestResponse;
import com.ticketsystem.entity.enums.AssignmentRequestStatus;

import java.util.List;

/** Atama isteği yönetimi iş mantığı arayüzü. */
public interface AssignmentRequestService {

    /**
     * Oturum açmış agent adına yeni bir atama isteği oluşturur.
     * Yalnızca status=NEW ve assignedTo=null olan ticket'lar için istek açılabilir.
     * Aynı (agent, ticket, PENDING) kombinasyonu varsa {@code BusinessRuleException} fırlatır.
     */
    AssignmentRequestResponse createRequest(CreateAssignmentRequestRequest request);

    /**
     * Belirtilen statüdeki atama isteklerini listeler.
     * status verilmezse varsayılan olarak PENDING döner.
     */
    List<AssignmentRequestResponse> getRequests(AssignmentRequestStatus status);

    /**
     * Belirtilen isteği onaylar.
     * Ticket ilgili agent'a atanır; aynı ticket'ın diğer PENDING istekleri REJECTED yapılır.
     * İstek PENDING değilse {@code BusinessRuleException} fırlatır.
     */
    AssignmentRequestResponse approveRequest(Long requestId);

    /**
     * Belirtilen isteği reddeder. Ticket'a dokunulmaz.
     * İstek PENDING değilse {@code BusinessRuleException} fırlatır.
     */
    AssignmentRequestResponse rejectRequest(Long requestId);
}
