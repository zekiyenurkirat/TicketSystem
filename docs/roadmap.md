# TicketSystem Roadmap

Bu roadmap, projenin adım adım ve anlaşılır şekilde geliştirilmesi için hazırlanmıştır.

## Faz 0 — Dokümantasyon Hazırlığı

Amaç:
Projeye başlamadan önce gereksinimleri, analizleri ve geliştirme kurallarını netleştirmek.

Yapılacaklar:
- Analiz dokümanı hazırlanacak.
- Teknik analiz dokümanı hazırlanacak.
- Proje gereksinimleri md dosyasına yazılacak.
- Toplantı notları md dosyasına yazılacak.
- Java öğrenme notları özetlenecek.
- AI kuralları hazırlanacak.
- Roadmap oluşturulacak.

Çıktılar:
- docs klasörü
- deliverables klasörü
- GitHub ilk commit

## Faz 1 — Backend Proje İskeleti

Amaç:
Java 21 ve Spring Boot 3.x ile temel backend projesini kurmak.

Yapılacaklar:
- Spring Boot projesi oluşturulacak.
- Maven dependency yapısı kurulacak.
- PostgreSQL dependency eklenecek.
- Spring Web eklenecek.
- Spring Data JPA eklenecek.
- Lombok eklenecek.
- Validation dependency eklenecek.
- Swagger/OpenAPI dependency eklenecek.
- Proje paket yapısı oluşturulacak.

Önerilen paket yapısı:
- controller
- service
- service.impl
- repository
- entity
- dto.request
- dto.response
- exception
- config
- core.response

## Faz 2 — Entity ve ORM İlişkileri

Amaç:
TicketSystem domain modelini oluşturmak.

Entityler:
- User
- Ticket
- Comment
- Attachment
- SlaRule

İlişkiler:
- User 1 - N Ticket
- Ticket N - 1 User createdBy
- Ticket N - 1 User assignedTo
- Ticket 1 - N Comment
- Comment N - 1 User author
- Ticket 1 - N Attachment
- Attachment N - 1 User uploadedBy

Kullanılacak JPA anotasyonları:
- @Entity
- @Table
- @Id
- @GeneratedValue
- @Column
- @Enumerated
- @ManyToOne
- @OneToMany
- @JoinColumn

## Faz 3 — Repository Katmanı

Amaç:
Veritabanı erişim katmanını oluşturmak.

Repositoryler:
- UserRepository
- TicketRepository
- CommentRepository
- AttachmentRepository
- SlaRuleRepository

Kurallar:
- Repository sadece veritabanı erişimi yapar.
- İş kuralı içermez.
- JpaRepository kullanılır.
- Gerekirse query methodları yazılır.

Örnek queryler:
- findByStatus
- findByPriority
- findByCreatedById
- findByAssignedToId
- findByTicketNumber

## Faz 4 — Service Katmanı

Amaç:
İş kurallarını yazmak.

Servisler:
- UserService
- TicketService
- CommentService
- AttachmentService
- SlaService

Service implementation:
- UserManager
- TicketManager
- CommentManager
- AttachmentManager
- SlaManager

Kurallar:
- İş kuralları service katmanında olur.
- Controller iş kuralı içermez.
- Repository doğrudan controllerdan çağrılmaz.
- Constructor injection kullanılır.

Ticket iş kuralları:
- Ticket oluşturma
- Ticket listeleme
- Ticket detayı
- Ticket statü geçişi
- Ticket atama
- Öncelik değiştirme
- SLA hesaplama
- Internal/external yorum kontrolü

## Faz 5 — DTO Yapısı

Amaç:
Entityleri doğrudan dış dünyaya açmamak.

Request DTO örnekleri:
- CreateTicketRequest
- UpdateTicketStatusRequest
- AssignTicketRequest
- CreateCommentRequest
- CreateUserRequest
- LoginRequest

Response DTO örnekleri:
- TicketResponse
- TicketDetailResponse
- CommentResponse
- UserResponse
- AuthResponse
- SlaStatusResponse

Kurallar:
- Controller entity değil DTO alır/döner.
- Entity veritabanı modelidir.
- DTO API modelidir.

## Faz 6 — Standart API Response

Amaç:
API cevaplarını standart hale getirmek.

Olası response yapısı:

- success
- message
- data
- timestamp
- path

Core response sınıfları:
- ApiResponse
- DataResponse
- ErrorResponse

Alternatif olarak Engin Demiroğ tarzı:
- Result
- DataResult
- SuccessResult
- ErrorResult
- SuccessDataResult
- ErrorDataResult

TicketSystem için modern ApiResponse yapısı tercih edilebilir.

## Faz 7 — Global Exception Handling

Amaç:
Hataları merkezi şekilde yönetmek.

Yapılacaklar:
- GlobalExceptionHandler oluşturulacak.
- ResourceNotFoundException yazılacak.
- BusinessException yazılacak.
- Validation hataları yakalanacak.
- Yetki hataları için uygun response döndürülecek.

Kurallar:
- Her yerde try-catch yazılmaz.
- Hatalar merkezi yönetilir.
- Kullanıcıya anlaşılır hata mesajı döner.

## Faz 8 — Controller Katmanı

Amaç:
REST API endpointlerini oluşturmak.

Controllerlar:
- AuthController
- UserController
- TicketController
- CommentController
- AttachmentController
- ReportController

Endpoint standardı:
- /api/v1/auth
- /api/v1/users
- /api/v1/tickets
- /api/v1/comments
- /api/v1/attachments
- /api/v1/reports

Kurallar:
- Controller sadece HTTP isteğini karşılar.
- İş kuralı service katmanına gider.
- DTO kullanılır.
- Response standardı korunur.

## Faz 9 — Swagger/OpenAPI

Amaç:
API dokümantasyonu ve test ortamı oluşturmak.

Yapılacaklar:
- SpringDoc OpenAPI eklenecek.
- Swagger UI aktif edilecek.
- Endpointler test edilecek.
- Request/response modelleri görünecek.

## Faz 10 — PostgreSQL ve Flyway

Amaç:
Veritabanı bağlantısı ve migration yönetimi.

Yapılacaklar:
- PostgreSQL bağlantısı yapılacak.
- application.yml veya application.properties düzenlenecek.
- Flyway eklenecek.
- İlk migration dosyaları yazılacak.

## Faz 11 — Authentication ve Authorization

Amaç:
Giriş ve rol bazlı yetkilendirme yapmak.

İlk aşama:
- JWT login
- JWT token üretme
- Password hashing
- Role based authorization

Roller:
- CUSTOMER
- AGENT
- MANAGER

İleri aşama:
- Keycloak integration
- 2FA desteği

## Faz 12 — React Frontend

Amaç:
Ticket sisteminin kullanıcı arayüzünü oluşturmak.

Ekranlar:
- Login
- Dashboard
- Ticket List
- Ticket Detail
- Create Ticket
- User Management
- Reports
- Profile

## Faz 13 — Docker

Amaç:
Projeyi kolay çalıştırılabilir hale getirmek.

Yapılacaklar:
- Backend Dockerfile
- Frontend Dockerfile
- docker-compose.yml
- PostgreSQL container
- Gerekirse OpenSearch, Kafka, Prometheus, Grafana containerları

## Faz 14 — jBPM

Amaç:
Ticket yaşam döngüsünü workflow olarak yönetmek.

Ticket workflow:
NEW → ASSIGNED → IN_PROGRESS → WAITING_FOR_CUSTOMER → RESOLVED → CLOSED

jBPM ile:
- Process definition
- Process instance
- User task
- Gateway
- Timer event
- SLA escalation

## Faz 15 — Logging ve Observability

Amaç:
Kurumsal izleme ve loglama altyapısı kurmak.

Yapılacaklar:
- Log4j2
- Kafka
- Log consumer service
- OpenSearch
- OpenTelemetry
- Prometheus
- Grafana

Log pipeline:
Log4j2 → Kafka → Consumer → OpenSearch

## Faz 16 — Unit Test

Amaç:
Kod kalitesini artırmak.

Testler:
- Service unit test
- Controller test
- Repository test
- Validation test

Araçlar:
- JUnit 5
- Mockito
- Spring Boot Test

## Faz 17 — README ve Sunum

Amaç:
Projeyi teslim edilebilir hale getirmek.

README içeriği:
- Proje amacı
- Kullanılan teknolojiler
- Kurulum adımları
- Docker ile çalıştırma
- API endpointleri
- Test kullanıcıları
- Eksik kalan isterler

Sunum:
- 10-12 slayt
- PPT formatında
- Kişisel sayfa
- Proje amacı
- Öğrenilenler
- Zorlayan bölümler
- Eksik kalan isterler