# Proje Gereksinimleri

## Proje Adı

TicketSystem

## Proje Konusu

IT Servis - Ticket Yönetimi uygulaması.

Bu proje, müşterilerin destek talebi oluşturabildiği, destek ekibinin bu talepleri işleyebildiği, yöneticilerin raporlama ve SLA takibi yapabildiği kurumsal bir IT Service Management sistemidir.

## Frontend Gereksinimleri

- ReactJS kullanılacaktır.
- Kullanıcı giriş ekranı olacaktır.
- Ticket listesi görüntülenecektir.
- Ticket detay ekranı olacaktır.
- Ticket oluşturma ekranı olacaktır.
- Role göre farklı ekran davranışları olacaktır.
- SLA durumları renkli gösterilecektir.

## Backend Gereksinimleri

- Java 21 tercih edilecektir.
- Spring Boot 3.x veya en güncel stabil sürüm kullanılacaktır.
- REST API mimarisi uygulanacaktır.
- Loglama için Log4j2 tercih edilecektir.

## Veritabanı ve ORM

- PostgreSQL kullanılacaktır.
- ORM için Spring Data JPA / Hibernate kullanılacaktır.
- Migration için Flyway veya Liquibase kullanılacaktır.

## Güvenlik

- Authentication ve Authorization yapılacaktır.
- İlk aşamada JWT tabanlı authentication kurulacaktır.
- Role Based Authorization uygulanacaktır.
- Roller:
  - CUSTOMER
  - AGENT
  - MANAGER
- Keycloak entegrasyonu ileri fazda yapılacaktır.
- 2FA desteği ileri fazda değerlendirilecektir.

## Gözlemlenebilirlik ve İzleme

- OpenTelemetry kullanılacaktır.
- Monitoring için Grafana + Prometheus hedeflenmektedir.
- Log/search altyapısı için OpenSearch kullanılacaktır.

## OpenSearch Log Aktarımı

Hedef log pipeline:

Log4j2 → Kafka → Log Consumer Service → OpenSearch

Bu yapı temel ticket sistemi çalıştıktan sonra eklenecektir.

## DevOps

- Docker kullanılacaktır.
- Docker Compose ile backend, frontend, PostgreSQL ve diğer servislerin ayağa kaldırılması hedeflenmektedir.
- GitHub repository oluşturulacaktır.
- Düzenli commit trafiği sağlanacaktır.

## İş Akışı Yönetimi

- IT Servis - Ticket Yönetimi projesi için jBPM yapılması beklenmektedir.
- jBPM, ticket yaşam döngüsünü yönetmek için kullanılacaktır.
- Ticket yaşam döngüsü:
  - NEW
  - ASSIGNED
  - IN_PROGRESS
  - WAITING_FOR_CUSTOMER
  - RESOLVED
  - CLOSED
  - CANCELLED

## Performans

- Cache mekanizması opsiyoneldir.
- İlk aşamada in-memory cache veya basit cache yaklaşımı değerlendirilebilir.
- Redis ileri fazda eklenebilir.

## Dokümantasyon ve Kalite

- REST API versioning standardı uygulanacaktır.
- Endpointler /api/v1/... formatında olacaktır.
- Swagger/OpenAPI kullanılacaktır.
- Javadocs eklenecektir.
- README dosyası detaylı hazırlanacaktır.
- Unit test yazılacaktır.
- Error Handling merkezi yapılacaktır.
- Katmanlı mimari ve OOP prensiplerine dikkat edilecektir.

## Analiz ve Sunum

Teslimde bulunması gerekenler:

- Güncellenmiş Analiz Dokümanı
- Güncellenmiş Teknik Analiz Dokümanı
- Sunum dosyası

Sunumda bulunması gerekenler:

- Kişisel sayfa
  - Not ortalaması
  - Kaçıncı sınıf
  - Bilinen yabancı diller ve seviyeleri
  - Mesleki ilgi alanları
- Projeden öğrenilenler
- Zorlayan bölümler
- Projenin amacı
- İstenen teknolojiler dışında kullanılan teknolojiler
- Eksik kalan isterler
- Sunum PDF değil, PPT veya benzeri formatta olmalıdır.
- Sunum 10-12 sayfayı geçmemelidir.

## Önceliklendirme

İlk hedef:
- Çalışan temel TicketSystem backend
- Entity ilişkileri
- Repository
- Service
- Controller
- DTO
- Error Handling
- Swagger
- PostgreSQL bağlantısı

Sonraki hedef:
- JWT
- React frontend
- Docker
- jBPM
- Log4j2
- Kafka
- OpenSearch
- Grafana
- Prometheus
- OpenTelemetry