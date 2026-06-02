package com.ticketsystem.service.impl;

import com.ticketsystem.core.exception.DuplicateResourceException;
import com.ticketsystem.core.exception.ResourceNotFoundException;
import com.ticketsystem.dto.request.CreateUserRequest;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.Role;
import com.ticketsystem.repository.UserRepository;
import com.ticketsystem.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** {@link UserService} arayüzünün varsayılan uygulaması. */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        User currentUser = getCurrentUser();
        if (currentUser != null && currentUser.getRole() == Role.CUSTOMER
                && !currentUser.getId().equals(id)) {
            throw new AccessDeniedException("Yalnızca kendi profilinizi görüntüleyebilirsiniz.");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı. id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        User currentUser = getCurrentUser();
        if (currentUser != null && currentUser.getRole() == Role.CUSTOMER
                && !currentUser.getEmail().equals(email)) {
            throw new AccessDeniedException("Yalnızca kendi profilinizi görüntüleyebilirsiniz.");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı. email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getActiveUsersByRole(Role role) {
        return userRepository.findByRoleAndActiveTrue(role);
    }

    @Override
    @Transactional
    public User deactivateUser(Long id) {
        User user = getUserById(id);
        user.setActive(false);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User createUserByManager(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Bu email adresi zaten kayıtlı: " + request.getEmail());
        }
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setActive(true);
        return userRepository.save(user);
    }

    private User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }
}
