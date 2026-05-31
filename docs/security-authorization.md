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
| `POST /api/v1/users/admin` | Yetkili kullanıcı oluşturma (AGENT, MANAGER veya CUSTOMER) |
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
| `PATCH /api/v1/tickets/{id}/priority-review` | Ticket'ın aktif önceliğini gözden geçirme ve güncelleme |

### Herkese Açık (permitAll) Endpointler

| Endpoint | Açıklama |
|----------|---------|
| `POST /api/v1/auth/login` | Giriş ve token alma |
| `POST /api/v1/users` | Public kayıt — yalnızca `CUSTOMER` rolü oluşturulabilir |
| `/swagger-ui/**`, `/v3/api-docs/**` | API dokümantasyonu |

> **Not:** `POST /api/v1/users` self-registration içindir ve yalnızca `CUSTOMER` oluşturur. `AGENT` veya `MANAGER` oluşturmak için `POST /api/v1/users/admin` kullanılmalıdır; bu endpoint MANAGER tokenı gerektirir.

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
| `POST /attachments/upload` | Yalnızca kendi ticket'ına upload; başkasının ticket'ı → 403 |
| `GET /attachments/{id}/download` | Yalnızca kendi ticket'ının dosyası; başkasının ticket'ı → 403 |
| `GET /attachments/ticket/{ticketId}` | Yalnızca kendi ticket'ının attachment'ları (ticket ownership zinciri) |
| `GET /attachments/uploader/{userId}` | Yalnızca kendi `userId`; başkasının → 403 |

---

## Attachment Upload ve Download Güvenliği

### Endpointler

| Endpoint | Method | Content-Type | Açıklama |
|----------|--------|--------------|---------|
| `/api/v1/attachments/upload` | `POST` | `multipart/form-data` | Ticket'a gerçek dosya yükleme; `ticketId` (form parametresi) ve `file` (multipart part) zorunludur. JWT token gerekli. |
| `/api/v1/attachments/{id}/download` | `GET` | — | Attachment'ı binary stream olarak indirme. `ApiResponse` wrapper kullanılmaz; `ResponseEntity<Resource>` döner. Hata durumlarında (401, 403, 404) `GlobalExceptionHandler` standart `ApiResponse.error()` formatında yanıt üretir. JWT token gerekli. |

### Sunucu Tarafında Belirlenen Alanlar

Dosya yüklenirken aşağıdaki alanların tümü sunucu tarafında atanır; client'ın gönderdiği değerler işleme alınmaz:

| Alan | Kaynak |
|------|--------|
| `uploadedBy` | JWT token'daki kimlik — `SecurityContextHolder` üzerinden alınır |
| `fileName` | `MultipartFile.getOriginalFilename()` — multipart header'ından |
| `fileType` | `MultipartFile.getContentType()` — doğrulanmış MIME |
| `fileSize` | `MultipartFile.getSize()` — sunucu tarafı ölçüm |
| `filePath` | UUID.ext formatında üretilen depolama adı |

> **Güvenlik notu:** `filePath` alanı hiçbir API yanıtına dahil edilmez. Client, dosyanın fiziksel depolama yolunu asla göremez. İndirme işlemi yalnızca attachment ID üzerinden `GET /api/v1/attachments/{id}/download` endpoint'i ile yapılır.

### Dosya Yükleme Kısıtlamaları

Yükleme isteği iki katmanda doğrulanır:

- **Spring parse katmanı:** İstek boyutu `10 MB` limitini aşarsa multipart parse aşamasında reddedilir; servis katmanına ulaşmaz.
- **Servis katmanı:** Boyut, uzantı, MIME type ve dosya adı güvenlik kontrolleri uygulanır.

| Kısıt | Sonuç | Mesaj |
|-------|-------|-------|
| Boş dosya | **400** | `"Yüklenecek dosya boş olamaz."` |
| Dosya boyutu > 10 MB | **400** | `"Dosya boyutu 10 MB sınırını aşamaz."` |
| Dosya adı boş | **400** | `"Dosya adı boş olamaz."` |
| Path traversal (`..`, `/`, `\`) içeren dosya adı | **400** | `"Güvensiz dosya adı."` |
| İzin verilmeyen uzantı | **400** | `"Bu dosya türüne izin verilmiyor."` |
| Uzantı ile MIME type uyumsuzluğu | **400** | `"Dosya türü ve uzantısı uyuşmuyor."` |

### İzin Verilen Dosya Türleri

Whitelist tabanlı kontrol uygulanır. Uzantı ve MIME type birebir eşleşmek zorundadır:

| Uzantı | İzin Verilen MIME |
|--------|------------------|
| `pdf` | `application/pdf` |
| `doc` | `application/msword` |
| `docx` | `application/vnd.openxmlformats-officedocument.wordprocessingml.document` |
| `png` | `image/png` |
| `jpg`, `jpeg` | `image/jpeg` |
| `txt` | `text/plain` |

`.exe`, `.bat`, `.sh`, `.jar`, `.msi`, `.zip` gibi bu listede yer almayan tüm uzantılar reddedilir.

### Fiziksel Depolama

- Fiziksel dosyalar `UUID.ext` formatında adlandırılır (örn: `a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf`). Client, orijinal dosya adını bu formattan tahmin edemez.
- DB'ye yalnızca `uuid.ext` kısa adı kaydedilir; mutlak yol yazılmaz. Bu sayede `app.upload.dir` konfigürasyonu değişse bile DB kayıtları geçerliliğini korur.
- Depolama dizini sürüm kontrolüne dahil edilmez.

---

## Priority Triage System

Ticket oluşturma ve öncelik yönetimi sürecinde beş ayrı öncelik kavramı birbirinden bağımsız olarak ele alınır.

| Alan | Tip | Açıklama |
|------|-----|---------|
| `customerPriority` | `Priority` | Ticket oluştururken creator tarafından seçilen öncelik |
| `impact` | `Impact` | Sorunun iş süreçlerine etkisi (LOW / MEDIUM / HIGH) |
| `urgency` | `Urgency` | Sorunun ne kadar hızlı çözülmesi gerektiği (LOW / MEDIUM / HIGH) |
| `suggestedPriority` | `Priority` | `impact` + `urgency` matrisinden sistem tarafından hesaplanan öneri |
| `priority` | `Priority` | Aktif/final priority; SLA `dueDate` hesaplamasında kullanılan asıl değer |

### Başlangıç Değerleri

Ticket oluşturulduğunda `priority`, `customerPriority` değerinden başlar. `suggestedPriority` yalnızca AGENT ve MANAGER için bir bilgi kaynağıdır; aktif `priority`'yi otomatik olarak değiştirmez. AGENT veya MANAGER `priority-review` ile final priority kararını verir; bu işlemden sonra `dueDate` aktif `priority`'ye göre yeniden hesaplanır.

### Impact + Urgency Matrisi

| Impact \ Urgency | LOW | MEDIUM | HIGH |
|------------------|-----|--------|------|
| **LOW** | LOW | MEDIUM | HIGH |
| **MEDIUM** | MEDIUM | HIGH | CRITICAL |
| **HIGH** | HIGH | CRITICAL | BLOCKER |

### CUSTOMER Priority Kuralları

CUSTOMER ticket oluştururken `customerPriority` alanında yalnızca `LOW`, `MEDIUM` veya `HIGH` değerlerini seçebilir. `CRITICAL` veya `BLOCKER` seçilmesi durumunda servis katmanı `BusinessRuleException` fırlatır ve **400** döner. AGENT ve MANAGER bu kısıtlamaya tabi değildir; tüm `Priority` değerleriyle ticket oluşturabilir.

### Priority Review

`PATCH /api/v1/tickets/{id}/priority-review` endpointi AGENT ve MANAGER tarafından çağrılabilir. CUSTOMER bu endpoint'e erişemez (**403**). İstek `priority` (zorunlu) ve `reviewNote` (opsiyonel, max 1000 karakter) alanlarını alır.

Review sonrasında:

- `priority` → request'teki değere güncellenir
- `dueDate` → yeni `priority`'ye göre `SlaService` tarafından yeniden hesaplanır
- `priorityReviewNote`, `priorityReviewedAt`, `priorityReviewedBy` → set edilir
- `customerPriority` ve `suggestedPriority` → değişmeden korunur

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
| `POST /api/v1/attachments/upload` | Kendi ticket'ına multipart dosya yükleme |
| `GET /api/v1/attachments/{id}/download` | Kendi ticket'ının dosyasını indirme |
| `GET /api/v1/attachments/ticket/{ticketId}` | Kendi ticket'ının attachment listesi |
| `GET /api/v1/attachments/uploader/{userId}` | Yalnızca kendi `userId` |

---

## CUSTOMER Kısıtları

| Girişim | Sonuç | Açıklama |
|---------|-------|---------|
| Başkasının ticket'ını `GET /{id}` ile görme | **403** | Servis ownership kontrolü |
| Başkasının ticket listesini `GET /created-by` ile çekme | **403** | Servis ownership kontrolü |
| Başkasının ticket statüsünü değiştirme | **403** | Ownership zinciri devreye girer |
| `RESOLVED→CLOSED` dışında statü geçişi | **400** | İş kuralı ihlali |
| Başka kullanıcı adına ticket oluşturma (`createdById` manipülasyonu) | **403** | Servis ownership kontrolü |
| `role=AGENT` veya `role=MANAGER` ile public kayıt (`POST /users`) | **400** | İş kuralı ihlali |
| `POST /api/v1/users/admin` endpointini çağırma | **403** | SecurityConfig MANAGER-only URL kuralı |
| Başkasının profilini ID ile görme | **403** | Servis ownership kontrolü |
| Başkasının profilini email ile görme | **403** | Servis ownership kontrolü |
| Başkasının ticket yorumlarını görme | **403** | Ticket ownership zinciri |
| Başka `authorId` ile yorum listesi çekme | **403** | Servis ownership kontrolü |
| INTERNAL yorum ekleme | **400** | İş kuralı ihlali |
| Başkasının ticket attachment'larını görme | **403** | Ticket ownership zinciri |
| Başkasının ticket dosyasını `GET /{id}/download` ile indirme | **403** | Ticket ownership zinciri |
| Başka `uploaderId` ile attachment listesi çekme | **403** | Servis ownership kontrolü |
| URL bazlı AGENT/MANAGER endpoint'leri | **403** | SecurityConfig URL kuralı |
| `customerPriority=CRITICAL` veya `BLOCKER` ile ticket oluşturma | **400** | İş kuralı ihlali |
| `PATCH /api/v1/tickets/{id}/priority-review` çağırma | **403** | SecurityConfig URL kuralı |

---

## AGENT Yetkileri

- Tüm ticket'ları listeleme, görüntüleme ve geçerli statü geçişlerini yapma
- EXTERNAL + INTERNAL yorum ekleme ve listeleme
- Herhangi ticket'a `POST /api/v1/attachments/upload` ile dosya yükleme (ownership kısıtsız)
- Herhangi attachment'ı `GET /api/v1/attachments/{id}/download` ile indirme (ownership kısıtsız)
- Tüm ticket ve kullanıcı attachment'larını listeleme
- `GET /api/v1/users/role/{role}/active` — aktif kullanıcı listesi
- Herhangi bir `createdById` ile ticket oluşturma
- `PATCH /api/v1/tickets/{id}/priority-review` — ticket'ın aktif önceliğini review edebilir; `dueDate` yeniden hesaplanır

AGENT kullanıcı oluşturma konusunda kısıtlıdır: `POST /api/v1/users/admin` çağrılamaz → **403**. AGENT, kendi başına veya başka bir AGENT ya da MANAGER hesabı oluşturamaz.

---

## MANAGER Yetkileri

AGENT'ın tüm yetkileri artı:

- `POST /api/v1/users/admin` — `AGENT`, `MANAGER` veya `CUSTOMER` rolünde yeni kullanıcı oluşturma
- `GET /api/v1/users/role/{role}` — role göre tüm kullanıcıları listeleme (aktif + pasif)
- `PATCH /api/v1/users/{id}/deactivate` — kullanıcıyı pasife alma
- `PATCH /api/v1/tickets/{id}/assign` — ticket'ı bir AGENT'a atama
- `PATCH /api/v1/tickets/{id}/priority-review` — ticket'ın aktif önceliğini review edebilir; `dueDate` yeniden hesaplanır

---

## HTTP Response Code Stratejisi

| Kod | Durum | Kaynak |
|-----|-------|--------|
| **401** | Token yok, geçersiz veya süresi dolmuş | `AuthenticationEntryPoint` |
| **403** | Rol yetersiz (URL kuralı ihlali) | `AccessDeniedHandler` |
| **403** | Başkasının verisine erişim (ownership ihlali) | `GlobalExceptionHandler` → `AccessDeniedException` |
| **400** | İş kuralı ihlali: geçersiz statü geçişi, yanlış rol, INTERNAL yorum, CUSTOMER tarafından CRITICAL/BLOCKER priority seçimi, dosya validasyon ihlali (boş dosya, boyut aşımı, izin verilmeyen uzantı, MIME uyumsuzluğu, path traversal) | `GlobalExceptionHandler` → `BusinessRuleException` veya `MaxUploadSizeExceededException` |
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
| MANAGER | `POST /users/admin` `role=AGENT` | **201** | AGENT kullanıcı oluşturma başarılı |
| MANAGER | `POST /users/admin` `role=MANAGER` | **201** | MANAGER kullanıcı oluşturma başarılı |
| AGENT | `POST /users/admin` | **403** | MANAGER-only URL kuralı |
| CUSTOMER | `POST /users/admin` | **403** | MANAGER-only URL kuralı |
| Herhangi | Token olmadan `POST /users/admin` | **401** | Authentication katmanı |
| Herhangi | `POST /users` `role=AGENT` | **400** | Public kayıt CUSTOMER-only kısıtı korunuyor |
| CUSTOMER | `POST /tickets` `customerPriority=CRITICAL` | **400** | CUSTOMER CRITICAL priority seçemez |
| CUSTOMER | `POST /tickets` `customerPriority=BLOCKER` | **400** | CUSTOMER BLOCKER priority seçemez |
| CUSTOMER | `POST /tickets` `LOW + impact=HIGH + urgency=HIGH` | **201**, `suggestedPriority=BLOCKER`, `priority=LOW` | Matrix hesaplama; aktif `priority` değişmez |
| CUSTOMER | `POST /tickets` `HIGH + impact=LOW + urgency=LOW` | **201**, `suggestedPriority=LOW`, `priority=HIGH` | Matrix hesaplama; aktif `priority` değişmez |
| AGENT | `POST /tickets` `customerPriority=CRITICAL` | **201** | AGENT priority kısıtlamasına tabi değil |
| AGENT | `PATCH /tickets/{id}/priority-review` `BLOCKER` | **200** | Review alanları set; `dueDate` güncellendi |
| MANAGER | `PATCH /tickets/{id}/priority-review` `CRITICAL` | **200** | `priorityReviewedBy` manager; `dueDate` güncellendi |
| CUSTOMER | `PATCH /tickets/{id}/priority-review` | **403** | SecurityConfig URL kuralı |
| Herhangi | Token olmadan `PATCH /tickets/{id}/priority-review` | **401** | Authentication katmanı |
| AGENT | `GET /tickets/priority/BLOCKER` | **200** | Aktif `priority`'ye göre filtre doğru çalışıyor |
| CUSTOMER | `POST /attachments/upload` (kendi ticket'ı, geçerli .txt) | **201** | Upload başarılı; `filePath` response'ta görünmez |
| CUSTOMER | `POST /attachments/upload` (başkasının ticket'ı) | **403** | Ticket ownership zinciri devreye girer |
| AGENT | `POST /attachments/upload` (herhangi ticket) | **201** | AGENT ownership kısıtsız |
| MANAGER | `POST /attachments/upload` (herhangi ticket) | **201** | MANAGER ownership kısıtsız |
| Anonim | `POST /attachments/upload` (token yok) | **401** | Authentication katmanı |
| CUSTOMER | `GET /attachments/{id}/download` (kendi dosyası) | **200** | Binary stream; `Content-Disposition` orijinal dosya adıyla |
| CUSTOMER | `GET /attachments/{id}/download` (başkasının ticket dosyası) | **403** | Ticket ownership zinciri devreye girer |
| AGENT | `GET /attachments/{id}/download` (herhangi attachment) | **200** | AGENT ownership kısıtsız |
| MANAGER | `GET /attachments/{id}/download` (herhangi attachment) | **200** | MANAGER ownership kısıtsız |
| Anonim | `GET /attachments/{id}/download` (token yok) | **401** | Authentication katmanı |
| Herhangi | `POST /attachments/upload` `.exe` uzantılı dosya | **400** | `"Bu dosya türüne izin verilmiyor."` |
| Herhangi | `POST /attachments/upload` boş dosya | **400** | `"Yüklenecek dosya boş olamaz."` |
| Herhangi | `POST /attachments/upload` 11 MB dosya | **400** | `"Dosya boyutu 10 MB sınırını aşamaz."` |
| Herhangi | `POST /attachments/upload` path traversal dosya adı (`../../evil.txt`) | **400** | `"Güvensiz dosya adı."` |
| Herhangi | `POST /attachments/upload` MIME/uzantı uyumsuzluğu (`.txt` + `image/png`) | **400** | `"Dosya türü ve uzantısı uyuşmuyor."` |
| Herhangi | `GET /attachments/99999/download` (var olmayan ID) | **404** | `"Attachment bulunamadı. id: 99999"` |

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

## İlk MANAGER Hesabı

`POST /api/v1/users` public endpoint yalnızca `CUSTOMER` oluşturur. `POST /api/v1/users/admin` ise MANAGER tokenı gerektirir. Bu tasarım gereği, sistemdeki ilk MANAGER hesabı uygulama üzerinden oluşturulamaz.

Gerçek sistemlerde ilk MANAGER hesabı şu yollardan biriyle oluşturulur:

- **Seed data / migration:** Uygulama başlangıcında veritabanına BCrypt hashli kayıt eklenir.
- **Bootstrap script:** Tek seferlik çalışan yönetici kayıt akışı.
- **Merkezi kimlik yönetimi:** Keycloak, Active Directory gibi harici sistemden sağlanır.
- **Doğrudan veritabanı kaydı:** Geliştirme ortamında DBA tarafından eklenir.

Bu projenin geliştirme ortamında ilk MANAGER hesabı doğrudan veritabanına eklenerek oluşturulmuş ve tüm testler bu hesapla doğrulanmıştır. Kod içine hardcoded admin bilgisi eklenmez.

---

## Bilinen Kısıtlamalar ve Gelecek Çalışmalar

| Konu | Açıklama |
|------|---------|
| `requesterId` query param | `GET /comments/ticket/{id}?requesterId=X` imzasında kalıyor; artık güvenlik kararı için kullanılmıyor ama interface/controller'dan kaldırılmadı. Ayrı refactoring fazına bırakıldı. |
| `addComment.authorId` | Request body'den alınıyor; token'dan otomatik atanabilir. |
| İlk MANAGER hesabı | `POST /api/v1/users/admin` yalnızca MANAGER tokenıyla erişilebilir. İlk MANAGER hesabı bootstrap, seed data veya doğrudan veritabanı kaydıyla oluşturulmalıdır. Kod içine hardcoded admin bilgisi eklenmemelidir. |
| `isActive` tutarlılığı | Pasif kullanıcının tüm işlemlerden engellenmesi için kontroller genişletilebilir. |
| Rate limiting | Brute-force koruması production öncesi eklenebilir. |
| Keycloak entegrasyonu | İleride planlanmaktadır; mevcut JWT altyapısı geçiş için hazır. |

---

## Özet

TicketSystem'de JWT authentication altyapısının üzerine iki katmanlı yetkilendirme eklendi. `SecurityFilterChain` ile URL bazlı rol kuralları ve servis katmanında `SecurityContextHolder` tabanlı ownership kontrolleri bir arada çalışarak CUSTOMER, AGENT ve MANAGER rollerinin güvenli veri erişim sınırları tanımlandı. Priority Triage System ile CUSTOMER priority seçimi kısıtlanmış; AGENT ve MANAGER ise `priority-review` endpointi üzerinden ticket'ların aktif önceliğini güncelleyebilir ve `dueDate`'i yeniden hesaplayabilir hale gelmiştir. Gerçek dosya yükleme ve indirme işlemleri `multipart/form-data` ile desteklenmekte; `uploadedBy`, `fileName`, `fileType`, `fileSize` ve `filePath` alanlarının tümü sunucu tarafında belirlenmekte, fiziksel depolama yolu client'a hiçbir zaman açılmamaktadır. Tüm kontroller runtime testlerle doğrulandı.
