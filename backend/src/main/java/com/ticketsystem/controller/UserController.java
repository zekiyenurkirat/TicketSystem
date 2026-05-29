package com.ticketsystem.controller;

import com.ticketsystem.core.response.ApiResponse;
import com.ticketsystem.dto.request.CreateUserRequest;
import com.ticketsystem.dto.response.UserResponse;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.Role;
import com.ticketsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Kullanıcı yönetimi HTTP isteklerini karşılar. */
@Tag(name = "Kullanıcı Yönetimi", description = "Kullanıcı oluşturma, sorgulama ve pasife alma işlemleri")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Yeni kullanıcı oluşturur. */
    @Operation(summary = "Yeni kullanıcı oluşturur",
               description = "Sistemde yeni bir kullanıcı kaydı oluşturur. E-posta adresi benzersiz olmalıdır.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Kullanıcı başarıyla oluşturuldu."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validasyon hatası veya geçersiz istek."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Bu e-posta adresiyle kayıtlı kullanıcı zaten mevcut."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @RequestBody @Valid CreateUserRequest request) {
        User user = userService.createUser(request);
        UserResponse response = UserResponse.from(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Kullanıcı başarıyla oluşturuldu."));
    }

    /** ID ile kullanıcı getirir. */
    @Operation(summary = "ID ile kullanıcı getirir",
               description = "Belirtilen ID'ye sahip kullanıcıyı döner.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Kullanıcı başarıyla getirildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Belirtilen ID'ye sahip kullanıcı bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        UserResponse response = UserResponse.from(user);
        return ResponseEntity.ok(ApiResponse.success(response, "Kullanıcı getirildi."));
    }

    /** E-posta ile kullanıcı getirir. */
    @Operation(summary = "E-posta ile kullanıcı getirir",
               description = "Belirtilen e-posta adresine sahip kullanıcıyı döner.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Kullanıcı başarıyla getirildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Belirtilen e-posta adresine sahip kullanıcı bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        UserResponse response = UserResponse.from(user);
        return ResponseEntity.ok(ApiResponse.success(response, "Kullanıcı getirildi."));
    }

    /** Role göre kullanıcıları getirir. */
    @Operation(summary = "Role göre kullanıcıları getirir",
               description = "Belirtilen role sahip tüm kullanıcıları döner.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Kullanıcı listesi başarıyla getirildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(@PathVariable Role role) {
        List<UserResponse> responses = userService.getUsersByRole(role)
                .stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses, "Kullanıcılar getirildi."));
    }

    /** Role göre aktif kullanıcıları getirir. */
    @Operation(summary = "Role göre aktif kullanıcıları getirir",
               description = "Belirtilen role sahip yalnızca aktif kullanıcıları döner.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Aktif kullanıcı listesi başarıyla getirildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
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
    @Operation(summary = "Kullanıcıyı pasife alır",
               description = "Belirtilen ID'ye sahip kullanıcının active alanını false yapar.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Kullanıcı başarıyla pasife alındı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Belirtilen ID'ye sahip kullanıcı bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(@PathVariable Long id) {
        User user = userService.deactivateUser(id);
        UserResponse response = UserResponse.from(user);
        return ResponseEntity.ok(ApiResponse.success(response, "Kullanıcı pasife alındı."));
    }
}
