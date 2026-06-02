package com.ticketsystem.service.impl;

import com.ticketsystem.core.exception.BusinessRuleException;
import com.ticketsystem.core.exception.DuplicateResourceException;
import com.ticketsystem.core.exception.ResourceNotFoundException;
import com.ticketsystem.dto.request.CreateRegistrationRequestRequest;
import com.ticketsystem.dto.response.RegistrationRequestResponse;
import com.ticketsystem.entity.RegistrationRequest;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.NotificationType;
import com.ticketsystem.entity.enums.RegistrationRequestStatus;
import com.ticketsystem.entity.enums.Role;
import com.ticketsystem.repository.RegistrationRequestRepository;
import com.ticketsystem.repository.UserRepository;
import com.ticketsystem.service.NotificationService;
import com.ticketsystem.service.RegistrationRequestService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** {@link RegistrationRequestService} arayüzünün varsayılan uygulaması. */
@Service
public class RegistrationRequestServiceImpl implements RegistrationRequestService {

    private final RegistrationRequestRepository registrationRequestRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public RegistrationRequestServiceImpl(
            RegistrationRequestRepository registrationRequestRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            NotificationService notificationService) {
        this.registrationRequestRepository = registrationRequestRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public RegistrationRequestResponse createRegistrationRequest(CreateRegistrationRequestRequest request) {
        String email = request.getEmail();

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Bu email adresiyle kayıtlı bir kullanıcı zaten mevcut: " + email);
        }

        // PENDING veya APPROVED kayıt talebi varsa yeni talep açılmaz
        if (registrationRequestRepository.existsByEmailAndStatusIn(
                email, List.of(RegistrationRequestStatus.PENDING, RegistrationRequestStatus.APPROVED))) {
            throw new BusinessRuleException("Bu email adresi için bekleyen veya onaylanmış bir kayıt talebi zaten mevcut: " + email);
        }

        RegistrationRequest regRequest = new RegistrationRequest();
        regRequest.setFirstName(request.getFirstName());
        regRequest.setLastName(request.getLastName());
        regRequest.setEmail(email);
        regRequest.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        regRequest.setRequestedRole(Role.CUSTOMER);
        regRequest.setStatus(RegistrationRequestStatus.PENDING);

        RegistrationRequest saved = registrationRequestRepository.save(regRequest);

        String applicantFullName = request.getFirstName() + " " + request.getLastName();
        String message = applicantFullName + " (" + email + ") sisteme kayıt talebi oluşturdu.";
        userRepository.findByRoleAndActiveTrue(Role.MANAGER)
                .forEach(manager -> notificationService.createNotification(
                        manager.getId(),
                        NotificationType.REGISTRATION_REQUEST_CREATED,
                        message,
                        null));

        return RegistrationRequestResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationRequestResponse> getRegistrationRequests(RegistrationRequestStatus status) {
        return registrationRequestRepository.findByStatus(status)
                .stream()
                .map(RegistrationRequestResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public RegistrationRequestResponse approveRegistrationRequest(Long id) {
        RegistrationRequest regRequest = registrationRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kayıt talebi bulunamadı. id: " + id));

        if (regRequest.getStatus() != RegistrationRequestStatus.PENDING) {
            throw new BusinessRuleException("Yalnızca PENDING statüsündeki kayıt talepleri onaylanabilir.");
        }

        // Onay sırasında email çakışması son kez kontrol edilir
        if (userRepository.existsByEmail(regRequest.getEmail())) {
            throw new DuplicateResourceException(
                    "Bu email adresiyle kayıtlı bir kullanıcı zaten mevcut: " + regRequest.getEmail());
        }

        User newUser = new User();
        newUser.setFirstName(regRequest.getFirstName());
        newUser.setLastName(regRequest.getLastName());
        newUser.setEmail(regRequest.getEmail());
        newUser.setPasswordHash(regRequest.getPasswordHash());
        newUser.setRole(regRequest.getRequestedRole());
        newUser.setActive(true);
        User savedUser = userRepository.save(newUser);

        User manager = getCurrentManager();
        regRequest.setStatus(RegistrationRequestStatus.APPROVED);
        regRequest.setReviewedBy(manager);
        regRequest.setReviewedAt(LocalDateTime.now());
        RegistrationRequest savedRequest = registrationRequestRepository.save(regRequest);

        String message = "Kayıt talebiniz onaylandı. Artık sisteme giriş yapabilirsiniz.";
        notificationService.createNotification(
                savedUser.getId(),
                NotificationType.REGISTRATION_REQUEST_APPROVED,
                message,
                null);

        return RegistrationRequestResponse.from(savedRequest);
    }

    @Override
    @Transactional
    public RegistrationRequestResponse rejectRegistrationRequest(Long id, String note) {
        RegistrationRequest regRequest = registrationRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kayıt talebi bulunamadı. id: " + id));

        if (regRequest.getStatus() != RegistrationRequestStatus.PENDING) {
            throw new BusinessRuleException("Yalnızca PENDING statüsündeki kayıt talepleri reddedilebilir.");
        }

        User manager = getCurrentManager();
        regRequest.setStatus(RegistrationRequestStatus.REJECTED);
        regRequest.setNote(note);
        regRequest.setReviewedBy(manager);
        regRequest.setReviewedAt(LocalDateTime.now());
        RegistrationRequest saved = registrationRequestRepository.save(regRequest);

        // Hesap oluşturulmadığından bildirim gönderilebilecek bir kullanıcı mevcut değil.

        return RegistrationRequestResponse.from(saved);
    }

    private User getCurrentManager() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Oturum açmış kullanıcı bulunamadı."));
    }
}
