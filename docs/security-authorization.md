# Security & Authorization

## Genel Bakış

Spring Security 6 + JJWT 0.12.6 ile JWT tabanlı kimlik doğrulama kurulduktan sonra, role-based authorization ve ownership kontrolleri iki katmanlı olarak eklendi:

- **URL seviyesi:** `SecurityFilterChain` içinde rol bazlı kaba kısıtlar. Belirli endpointler yalnızca belirli rollere açık.
- **Servis seviyesi:** Her servis impl sınıfında `SecurityContextHolder` üzerinden token kullanıcısı alınarak veri sahipliği (ownership) kontrol edilir. Aynı role sahip iki kullanıcı aynı endpoint'i çağırabilir; ancak yalnızca kendi verisine erişebilir.

---

## Authentication ve Authorization Ayrımı

| Kavram | Tanım | Uygulanma Yeri |
|--------|-------|---------------|
| **Authentication** | Kullanıcının kim olduğunu doğrulama | `JwtAuthenticationFilter`, `CustomUserDetailsService` |
| **Authorization** | Kullanıcının hangi işlemi yapabileceğini ve hangi veriye erişebileceğini belirleme | `SecurityConfig` (URL) + ServiceImpl sınıfları (ownership) |

Her istek şu sırayı izler:

```
HTTP İsteği → JwtAuthenticationFilter → SecurityFilterChain (URL kuralı) → Controller → Service (ownership)
```

---

## Roller

| Rol | Açıklama |
|-----|---------|
| `CUSTOMER` | Destek talebi oluşturan son kullanıcı. Yalnızca kendi verilerine erişebilir. |
| `AGENT` | Talepleri işleyen destek personeli. Tüm ticketları görebilir, statü değiştirebilir, INTERNAL yorum ekleyebilir. |
| `MANAGER` | Sistemi yöneten yönetici. AGENT yetkilerine ek olarak kullanıcı yönetimi ve ticket atama yapabilir. |

---

## URL Seviyesi Yetkilendirme

`SecurityConfig` içinde tanımlı kurallar. Bu kurallara takılan istekler servise hiç ulaşmaz; 403 döner.

### MANAGER-only Endpointler

| Endpoint | Açıklama |
|----------|---------|
| `PATCH /api/v1/users/{id}/deactivate` | Kullanıcı pasife alma |
| `GET /api/v1/users/role/{role}` | Role göre tüm kullanıcıları listeleme |
| `PATCH /api/v1/tickets/{id}/assign` | Ticket atama |

### AGENT + MANAGER Endpointler

| Endpoint | Açıklama |
|----------|---------|
| `GET /api/v1/users/role/{role}/active` | Role göre aktif kullanıcıları listeleme |
| `GET /api/v1/tickets/status/{status}` | Statüye göre ticket listeleme |
| `GET /api/v1/tickets/priority/{priority}` | Önceliğe göre ticket listeleme |
| `GET /api/v1/tickets/unassigned` | Atanmamış ticketları listeleme |
| `GET /api/v1/tickets/filter` | Filtreli ticket listeleme |

### Herkese Açık (permitAll) Endpointler

| Endpoint | Açıklama |
|----------|---------|
| `POST /api/v1/auth/login` | Giriş ve token alma |
| `POST /api/v1/users` | Public kayıt (yalnızca CUSTOMER rolü oluşturulabilir) |
| `/swagger-ui/**`, `/v3/api-docs/**` | API dokümantasyonu |

### Kimlik Doğrulama Gerektiren Endpointler

Yukarıdaki kurallara girmeyen tüm endpointler: geçerli JWT token zorunlu.

---

## Servis Seviyesi Ownership Kontrolleri

URL kurallarından geçen istekler için servis katmanında `getCurrentUser()` helper'ı ile token'daki kimlik alınır ve veri sahipliği kontrol edilir.

### Ticket Ownership

| Endpoint | CUSTOMER Kontrolü |
|----------|------------------|
| `GET /tickets/{id}` | Yalnızca kendi ticket'ı; başkasının → 403 |
| `GET /tickets/number/{ticketNumber}` | Yalnızca kendi ticket'ı; başkasının → 403 |
| `GET /tickets/created-by/{userId}` | Yalnızca kendi `userId`; başkasının → 403 |
| `PATCH /tickets/{id}/status` | Yalnızca kendi ticket'ı + `RESOLVED→CLOSED` kısıtı |
| `POST /tickets` | `createdById` == token kullanıcısının ID'si zorunlu |

### User Profile Ownership

| Endpoint | CUSTOMER Kontrolü |
|----------|------------------|
| `GET /users/{id}` | Yalnızca kendi profili; başkasının → 403 |
| `GET /users/email/{email}` | Yalnızca kendi email'i; başkasının → 403 |
| `POST /users` | Yalnızca `role=CUSTOMER` ile kayıt; farklı rol → 400 |

### Comment Ownership

| Endpoint | CUSTOMER Kontrolü |
|----------|------------------|
| `GET /comments/ticket/{ticketId}` | Yalnızca kendi ticket'ının EXTERNAL yorumları; başkasının ticket'ı → 403 (ticket ownership zinciri) |
| `GET /comments/author/{authorId}` | Yalnızca kendi `authorId`; başkasının → 403 |
| `POST /comments` | Ticket ownership zinciri (kendi ticket'ına yorum); `INTERNAL` tip → 400 |

### Attachment Ownership

| Endpoint | CUSTOMER Kontrolü |
|----------|------------------|
| `GET /attachments/ticket/{ticketId}` | Yalnızca kendi ticket'ının attachment'ları (ticket ownership zinciri) |
| `GET /attachments/uploader/{userId}` | Yalnızca kendi `userId`; başkasının → 403 |
| `POST /attachments` | Ticket + uploader ownership zincirleri |

---

## CUSTOMER Yetkileri

| İşlem | Koşul |
|-------|-------|
| `POST /api/v1/auth/login` | Kısıtsız |
| `POST /api/v1/users` | Yalnızca `role=CUSTOMER` ile kayıt |
| `POST /api/v1/tickets` | `createdById` kendi ID'si olmalı |
| `GET /api/v1/tickets/{id}` | Yalnızca kendi ticket'ı |
| `GET /api/v1/tickets/number/{no}` | Yalnızca kendi ticket'ı |
| `GET /api/v1/tickets/created-by/{userId}` | Yalnızca kendi `userId` |
| `PATCH /api/v1/tickets/{id}/status` | Yalnızca kendi `RESOLVED` ticket'ını `CLOSED` yapmak |
| `GET /api/v1/users/{id}` | Yalnızca kendi profili |
| `GET /api/v1/users/email/{email}` | Yalnızca kendi email'i |
| `GET /api/v1/comments/ticket/{ticketId}` | Kendi ticket'ının yalnızca EXTERNAL yorumları |
| `GET /api/v1/comments/author/{authorId}` | Yalnızca kendi `authorId` |
| `POST /api/v1/comments` | Kendi ticket'ına, yalnızca EXTERNAL |
| `GET /api/v1/attachments/ticket/{ticketId}` | Kendi ticket'ının attachment'ları |
| `GET /api/v1/attachments/uploader/{userId}` | Yalnızca kendi `userId` |
| `POST /api/v1/attachments` | Kendi ticket'ı + kendi `uploadedById` |

---

## CUSTOMER Kısıtları

| Girişim | Sonuç | Açıklama |
|---------|-------|---------|
| Başkasının ticket'ını `GET /{id}` ile görme | **403** | Servis ownership kontrolü |
| Başkasının ticket listesini `GET /created-by` ile çekme | **403** | Servis ownership kontrolü |
| Başkasının ticket statüsünü değiştirme | **403** | Ownership zinciri devreye girer |
| `RESOLVED→CLOSED` dışında statü geçişi | **400** | İş kuralı ihlali |
| Başka kullanıcı adına ticket oluşturma (`createdById` manipülasyonu) | **403** | Servis ownership kontrolü |
| `role=AGENT` veya `role=MANAGER` ile public kayıt | **400** | İş kuralı ihlali |
| Başkasının profilini ID ile görme | **403** | Servis ownership kontrolü |
| Başkasının profilini email ile görme | **403** | Servis ownership kontrolü |
| Başkasının ticket yorumlarını görme | **403** | Ticket ownership zinciri |
| Başka `authorId` ile yorum listesi çekme | **403** | Servis ownership kontrolü |
| INTERNAL yorum ekleme | **400** | İş kuralı ihlali |
| Başkasının ticket attachment'larını görme | **403** | Ticket ownership zinciri |
| Başka `uploaderId` ile attachment listesi çekme | **403** | Servis ownership kontrolü |
| URL bazlı AGENT/MANAGER endpoint'leri | **403** | SecurityConfig URL kuralı |

---

## AGENT Yetkileri

- Tüm ticket'ları listeleme, görüntüleme ve geçerli statü geçişlerini yapma
- EXTERNAL + INTERNAL yorum ekleme ve listeleme
- Tüm ticket ve kullanıcı attachment'larını listeleme
- `GET /api/v1/users/role/{role}/active` — aktif kullanıcı listesi
- Herhangi bir `createdById` veya `uploadedById` ile kayıt oluşturma

---

## MANAGER Yetkileri

AGENT'ın tüm yetkileri artı:

- `GET /api/v1/users/role/{role}` — role göre tüm kullanıcıları listeleme (aktif + pasif)
- `PATCH /api/v1/users/{id}/deactivate` — kullanıcıyı pasife alma
- `PATCH /api/v1/tickets/{id}/assign` — ticket'ı bir AGENT'a atama

---

## HTTP Response Code Stratejisi

| Kod | Durum | Kaynak |
|-----|-------|--------|
| **401** | Token yok, geçersiz veya süresi dolmuş | `AuthenticationEntryPoint` |
| **403** | Rol yetersiz (URL kuralı ihlali) | `AccessDeniedHandler` |
| **403** | Başkasının verisine erişim (ownership ihlali) | `GlobalExceptionHandler` → `AccessDeniedException` |
| **400** | İş kuralı ihlali (geçersiz statü geçişi, yanlış rol, INTERNAL yorum) | `GlobalExceptionHandler` → `BusinessRuleException` |
| **404** | Kaynak bulunamadı | `GlobalExceptionHandler` → `ResourceNotFoundException` |
| **409** | Email adresi zaten kayıtlı | `GlobalExceptionHandler` → `DuplicateResourceException` |

Tüm hata yanıtları standart `ApiResponse.error(mesaj)` formatında döner.

---

## Kritik Runtime Test Senaryoları

Doğrulanan önemli güvenlik senaryoları:

| Aktör | İstek | Sonuç | Açıklama |
|-------|-------|-------|---------|
| CUSTOMER (id=2) | `GET /tickets/{başkasının id}` | **403** | Ownership zinciri devreye girer |
| CUSTOMER (id=2) | `GET /users/99999` (olmayan) | **403** | 404 bilgisi sızdırılmaz |
| CUSTOMER (id=2) | `POST /tickets` `createdById=3` | **403** | ID manipülasyonu kapalı |
| CUSTOMER (id=2) | `GET /comments/ticket/{kendi}?requesterId=3` | **200, EXTERNAL** | `requesterId` artık güvenlik kararı vermiyor; token rolü kullanılıyor |
| CUSTOMER (id=2) | `GET /comments/author/3` | **403** | Başka authorId yasak |
| CUSTOMER (id=2) | `GET /attachments/uploader/99999` | **403** | 404 bilgisi sızdırılmaz |
| Herhangi | `POST /users` `role=AGENT` | **400** | Public kayıt yalnızca CUSTOMER |
| Herhangi | Token olmadan istek | **401** | Authentication katmanı |
| AGENT | `GET /tickets/{herhangi}` | **200** | AGENT davranışı bozulmadı |
| MANAGER | `PATCH /users/{id}/deactivate` | **200** | MANAGER yetkisi çalışıyor |

---

## getCurrentUser Helper Yaklaşımı

Her servis impl sınıfında `SecurityContextHolder` üzerinden token kullanıcısı alınır:

| Sınıf | `getCurrentUser()` Yapısı | Gerekçe |
|-------|--------------------------|---------|
| `TicketServiceImpl` | `userService.getUserByEmail(email)` | Standart yaklaşım |
| `CommentServiceImpl` | `userService.getUserByEmail(email)` | Standart yaklaşım |
| `AttachmentServiceImpl` | `userService.getUserByEmail(email)` | Standart yaklaşım |
| `UserServiceImpl` | `userRepository.findByEmail(email)` (null-safe) | Döngü önlemi |

**`UserServiceImpl` neden farklı?**

`UserServiceImpl.getUserByEmail()` metodunun başında ownership kontrolü bulunmaktadır. Eğer `getCurrentUser()` içinde `this.getUserByEmail()` çağrılsaydı:

```
getCurrentUser() → getUserByEmail() → getCurrentUser() → getUserByEmail() → ...
```

Bu sonsuz döngü `StackOverflowError`'a yol açardı. Çözüm: `userRepository.findByEmail(email)` doğrudan çağrılır ve ownership kontrolü atlanır. Ayrıca `JwtAuthenticationFilter` her istekte `SecurityContextHolder` henüz boş olabilecekken `getUserByEmail()` çağırdığından `authentication == null` null-safe guard eklendi.

---

## Bilinen Kısıtlamalar ve Gelecek Çalışmalar

| Konu | Açıklama |
|------|---------|
| `requesterId` query param | `GET /comments/ticket/{id}?requesterId=X` imzasında kalıyor; artık güvenlik kararı için kullanılmıyor ama interface/controller'dan kaldırılmadı. Ayrı refactoring fazına bırakıldı. |
| `addComment.authorId` | Request body'den alınıyor; token'dan otomatik atanabilir. |
| `saveAttachment.uploadedById` | Request body'den alınıyor; token'dan otomatik atanabilir. |
| AGENT/MANAGER oluşturma | Şu an kimse AGENT veya MANAGER oluşturamuyor. MANAGER-only bir endpoint gerekli. |
| `isActive` tutarlılığı | Pasif kullanıcının tüm işlemlerden engellenmesi için kontroller genişletilebilir. |
| Rate limiting | Brute-force koruması production öncesi eklenebilir. |
| Keycloak entegrasyonu | İleride planlanmaktadır; mevcut JWT altyapısı geçiş için hazır. |

---

## Özet

TicketSystem'de JWT authentication altyapısının üzerine iki katmanlı yetkilendirme eklendi. `SecurityFilterChain` ile URL bazlı rol kuralları ve servis katmanında `SecurityContextHolder` tabanlı ownership kontrolleri bir arada çalışarak CUSTOMER, AGENT ve MANAGER rollerinin güvenli veri erişim sınırları tanımlandı. Tüm kontroller runtime testlerle doğrulandı.
