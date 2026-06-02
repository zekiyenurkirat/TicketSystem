package com.ticketsystem.service;

import com.ticketsystem.dto.request.CreateRegistrationRequestRequest;
import com.ticketsystem.dto.response.RegistrationRequestResponse;
import com.ticketsystem.entity.enums.RegistrationRequestStatus;

import java.util.List;

/** Kayıt talebi yönetimi iş kurallarını tanımlar. */
public interface RegistrationRequestService {

    /** Yeni kayıt talebi oluşturur. Rol sabit CUSTOMER'dır; API'den talep edilemez. */
    RegistrationRequestResponse createRegistrationRequest(CreateRegistrationRequestRequest request);

    /** Belirtilen statüdeki tüm kayıt taleplerini getirir. */
    List<RegistrationRequestResponse> getRegistrationRequests(RegistrationRequestStatus status);

    /** Kayıt talebini onaylar ve kullanıcı hesabını active=true olarak oluşturur. */
    RegistrationRequestResponse approveRegistrationRequest(Long id);

    /** Kayıt talebini reddeder. Kullanıcı hesabı oluşturulmaz. */
    RegistrationRequestResponse rejectRegistrationRequest(Long id, String note);
}
