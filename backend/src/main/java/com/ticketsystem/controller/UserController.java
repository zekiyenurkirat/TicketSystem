package com.ticketsystem.controller;

import com.ticketsystem.core.response.ApiResponse;
import com.ticketsystem.dto.request.CreateUserRequest;
import com.ticketsystem.dto.response.UserResponse;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.Role;
import com.ticketsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Kullanıcı yönetimi HTTP isteklerini karşılar. */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Yeni kullanıcı oluşturur. */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @RequestBody @Valid CreateUserRequest request) {
        User user = userService.createUser(request);
        UserResponse response = UserResponse.from(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Kullanıcı başarıyla oluşturuldu."));
    }

    /** ID ile kullanıcı getirir. */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        UserResponse response = UserResponse.from(user);
        return ResponseEntity.ok(ApiResponse.success(response, "Kullanıcı getirildi."));
    }

    /** Email ile kullanıcı getirir. */
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        UserResponse response = UserResponse.from(user);
        return ResponseEntity.ok(ApiResponse.success(response, "Kullanıcı getirildi."));
    }

    /** Belirtilen role sahip tüm kullanıcıları getirir. */
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(@PathVariable Role role) {
        List<UserResponse> responses = userService.getUsersByRole(role)
                .stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses, "Kullanıcılar getirildi."));
    }

    /** Belirtilen role sahip aktif kullanıcıları getirir. */
    @GetMapping("/role/{role}/active")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getActiveUsersByRole(
            @PathVariable Role role) {
        List<UserResponse> responses = userService.getActiveUsersByRole(role)
                .stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses, "Aktif kullanıcılar getirildi."));
    }

    /** Kullanıcıyı pasife alır. */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(@PathVariable Long id) {
        User user = userService.deactivateUser(id);
        UserResponse response = UserResponse.from(user);
        return ResponseEntity.ok(ApiResponse.success(response, "Kullanıcı pasife alındı."));
    }
}
