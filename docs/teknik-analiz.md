# TicketSystem Teknik Analiz Dokümanı

## Teknik Amaç

Bu doküman, TicketSystem projesinin teknik mimarisini, kullanılacak teknolojileri ve geliştirme yaklaşımını açıklar.

## Mimari Yaklaşım

Proje katmanlı mimari ile geliştirilecektir.

Temel katmanlar:

- Controller
- Service
- Repository
- Entity
- DTO
- Exception
- Config
- Security
- Core Response

## Backend Teknolojileri

- Java 21
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- Lombok
- Spring Validation
- Spring Security
- JWT
- Swagger/OpenAPI
- Log4j2
- JUnit 5
- Mockito

## Frontend Teknolojileri

- ReactJS
- React Router
- Axios
- Component bazlı yapı

## Veritabanı

PostgreSQL kullanılacaktır.

Temel tablolar:

- users
- tickets
- comments
- attachments
- sla_rules

## ORM

Spring Data JPA ve Hibernate kullanılacaktır.

Entity ilişkileri:

- User 1 - N Ticket
- Ticket N - 1 User createdBy
- Ticket N - 1 User assignedTo
- Ticket 1 - N Comment
- Comment N - 1 User author
- Ticket 1 - N Attachment
- Attachment N - 1 User uploadedBy

## Migration

Flyway kullanılacaktır.

Amaç:
- Veritabanı şemasını versiyonlamak
- Ortamlar arası aynı DB yapısını kurmak
- Manuel SQL karmaşasını azaltmak

## API Standardı

REST API kullanılacaktır.

Versiyonlama:
- /api/v1/...

Örnek:
- /api/v1/tickets
- /api/v1/users
- /api/v1/auth

## DTO Kullanımı

Entityler doğrudan dış dünyaya açılmayacaktır.

Request DTO:
- Kullanıcıdan alınacak verileri temsil eder.

Response DTO:
- Kullanıcıya dönecek verileri temsil eder.

## Error Handling

Global Exception Handling kullanılacaktır.

Hedef:
- Hataları merkezi yönetmek
- Controller içinde try-catch yazmamak
- Kullanıcıya standart hata response dönmek

Exception örnekleri:
- ResourceNotFoundException
- BusinessException
- ValidationException
- UnauthorizedException

## Security

İlk aşama:
- JWT Authentication
- Role Based Authorization

Roller:
- CUSTOMER
- AGENT
- MANAGER

İleri aşama:
- Keycloak integration
- 2FA

## Swagger/OpenAPI

Swagger UI ile API dokümantasyonu sağlanacaktır.

Amaç:
- Endpointleri görmek
- Request/response modellerini test etmek
- Backend API'yi frontend geliştiriciler için anlaşılır yapmak

## jBPM

jBPM, ticket yaşam döngüsünü BPMN tabanlı workflow olarak yönetmek için kullanılacaktır.

Ticket workflow:

NEW → ASSIGNED → IN_PROGRESS → WAITING_FOR_CUSTOMER → RESOLVED → CLOSED

jBPM bileşenleri:
- Process Definition
- Process Instance
- User Task
- Gateway
- Timer Event
- SLA escalation

## Logging

Log4j2 kullanılacaktır.

Amaç:
- Uygulama loglarını standartlaştırmak
- Hata ve işlem loglarını takip etmek
- Kurumsal log pipeline için temel oluşturmak

## Kafka ve OpenSearch

Hedef pipeline:

Log4j2 → Kafka → Log Consumer Service → OpenSearch

Amaç:
- Logları Kafka topic'ine göndermek
- Consumer servis ile logları almak
- OpenSearch üzerinde aranabilir hale getirmek

Bu yapı temel sistem çalıştıktan sonra eklenecektir.

## Monitoring

Prometheus ve Grafana kullanılacaktır.

Prometheus:
- Metrik toplar.

Grafana:
- Dashboard ile metrikleri görselleştirir.

OpenTelemetry:
- Trace ve observability sağlar.

## Docker

Docker ve Docker Compose kullanılacaktır.

Amaç:
- Projeyi kolay çalıştırmak
- PostgreSQL, backend, frontend ve diğer servisleri tek komutla ayağa kaldırmak

## Unit Test

JUnit 5 ve Mockito kullanılacaktır.

Test stratejisi:
- Service unit test
- Controller test
- Repository test

Mock kullanımı:
- Service test edilirken gerçek repository kullanılmaz.
- Controller test edilirken gerçek service kullanılmaz.

## Geliştirme Önceliği

Önce:
- Backend çekirdeği
- Entity
- Repository
- Service
- Controller
- DTO
- Error Handling
- Swagger

Sonra:
- JWT
- React
- Docker
- jBPM
- Kafka
- OpenSearch
- Monitoring

## Eksik Kalabilecek İsterler

Eksik kalan isterler sunumda açıkça belirtilecektir.

Özellikle zaman durumuna göre ileri faza kalabilecekler:

- Keycloak
- 2FA
- Kafka pipeline
- OpenSearch
- Grafana + Prometheus
- OpenTelemetry
- Redis cache