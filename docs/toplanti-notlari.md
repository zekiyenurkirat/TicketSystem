# Toplantı Notları

## Proje Konusu

IT Service Management / Ticket Yönetimi uygulaması geliştirilecektir.

## Yapılacak Proje

Mailde iki proje başlığı geçmektedir:
- Finans Portalı
- IT Servis - Ticket Yönetimi

Bu repository içinde geliştirilecek proje:
- IT Servis - Ticket Yönetimi

## Referans Sistemler

Toplantıda Bugzilla benzeri bir yapı incelenmesi istenmiştir.

İncelenecek sistemler:
- Bugzilla
- Jira
- Kurumsal ITSM ticket sistemleri

## Bugzilla'dan Alınacak Fikirler

- Ticket oluşturma
- Ticket statüsü
- Öncelik seviyesi
- Atanan kişi
- Yorum sistemi
- Private/Internal yorum mantığı
- Resolution / kapanış mantığı
- Filtreleme
- Arama
- Ticket geçmişi

## Temel Roller

### CUSTOMER

- Ticket açar.
- Kendi ticketlarını görüntüler.
- Ticket detayını takip eder.
- External yorum yazar.
- Dosya ekleyebilir.

### AGENT

- Kendisine veya grubuna atanmış ticketları görür.
- Ticket statüsünü değiştirir.
- Internal ve external yorum yazabilir.
- Ticket çözüm sürecini yönetir.
- Worklog girebilir.

### MANAGER

- Tüm ticketları görür.
- Kullanıcı yönetimi yapar.
- SLA raporlarını görür.
- Agent performansını inceler.
- Eskalasyonları takip eder.

## Ticket Yaşam Döngüsü

Temel akış:

NEW → ASSIGNED → IN_PROGRESS → WAITING_FOR_CUSTOMER → RESOLVED → CLOSED

Ek durum:

CANCELLED

## Beklenen Temel Özellikler

- Kullanıcı girişi
- Rol bazlı yetkilendirme
- Ticket oluşturma
- Ticket listeleme
- Ticket detayı
- Ticket statü değiştirme
- Ticket atama
- Yorum ekleme
- Internal / external yorum ayrımı
- Dosya yükleme
- SLA takibi
- Raporlama
- Swagger/OpenAPI
- README
- Unit test
- Docker desteği
- GitHub commit düzeni

## Kurumsal Beklentiler

- Katmanlı mimari
- OOP prensipleri
- Temiz kod
- DTO kullanımı
- Global Exception Handling
- REST API versioning
- PostgreSQL
- Spring Boot 3.x
- Java 21
- ReactJS frontend
- jBPM workflow
- Kafka + OpenSearch log pipeline
- Grafana + Prometheus monitoring

## Strateji

Tüm kurumsal teknolojiler aynı anda eklenmeyecektir.

Önce çekirdek TicketSystem yapılacaktır:

1. Backend iskeleti
2. Entity ilişkileri
3. Repository
4. Service
5. Controller
6. DTO
7. Error Handling
8. Swagger
9. PostgreSQL
10. JWT

Daha sonra kurumsal eklemeler yapılacaktır:

1. React
2. Docker
3. jBPM
4. Log4j2
5. Kafka
6. OpenSearch
7. Grafana
8. Prometheus
9. OpenTelemetry
10. Keycloak
11. 2FA

## Teslimde Dikkat Edilecekler

- GitHub repository düzenli olmalıdır.
- Commit geçmişi görülmelidir.
- README dosyası detaylı olmalıdır.
- Proje nasıl çalıştırılır açıkça yazılmalıdır.
- Eksik kalan isterler sunumda dürüstçe belirtilmelidir.
- Sunum PDF değil PPT formatında olmalıdır.