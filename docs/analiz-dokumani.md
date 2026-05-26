# TicketSystem Analiz Dokümanı

## Proje Amacı

TicketSystem, müşterilerin teknik destek talepleri oluşturabildiği, destek personelinin bu talepleri SLA kuralları ile yönetebildiği ve yöneticilerin performans raporlarını inceleyebildiği kurumsal bir IT Service Management uygulamasıdır.

## Kapsam

- Ticket oluşturma
- Ticket listeleme
- Ticket detay görüntüleme
- Ticket statü yönetimi
- Ticket atama
- Yorum ekleme
- Internal / external yorum ayrımı
- Dosya yükleme
- SLA takibi
- Raporlama
- Rol bazlı yetkilendirme
- jBPM ile workflow yönetimi
- Swagger/OpenAPI dokümantasyonu

## Kapsam Dışı

- Fatura ve ödeme işlemleri
- Gerçek zamanlı canlı sohbet
- Üçüncü parti CRM entegrasyonu

## Roller

### CUSTOMER

- Ticket açabilir.
- Kendi ticketlarını görebilir.
- Ticket detayını takip edebilir.
- External yorum yazabilir.
- Dosya yükleyebilir.

### AGENT

- Atanmış ticketları görebilir.
- Ticket statüsünü değiştirebilir.
- Internal ve external yorum yazabilir.
- Ticket çözüm sürecini yönetebilir.
- Worklog girebilir.

### MANAGER

- Tüm ticketları görebilir.
- Kullanıcı yönetimi yapabilir.
- SLA raporlarını görebilir.
- Agent performansını takip edebilir.
- Eskalasyonları inceleyebilir.

## Ticket Yaşam Döngüsü

NEW → ASSIGNED → IN_PROGRESS → WAITING_FOR_CUSTOMER → RESOLVED → CLOSED

Ek durum:
- CANCELLED

## SLA Kuralları

Öncelikler:

- BLOCKER
- CRITICAL
- HIGH
- MEDIUM
- LOW

SLA sistemi:
- İlk müdahale süresi
- Çözüm süresi
- Eskalasyon süresi
- SLA ihlal uyarısı
- Manager bildirimi

## Temel Entityler

- User
- Ticket
- Comment
- Attachment
- SlaRule

## Temel Ekranlar

- Login
- Dashboard
- Ticket List
- Ticket Detail
- Create Ticket
- User Management
- SLA Settings
- Reports
- Notifications
- Profile

## API Standardı

Tüm endpointler /api/v1/... standardında olacaktır.

Örnek endpointler:

- POST /api/v1/auth/login
- GET /api/v1/auth/me
- POST /api/v1/tickets
- GET /api/v1/tickets
- GET /api/v1/tickets/my
- GET /api/v1/tickets/{id}
- PATCH /api/v1/tickets/{id}/status
- PATCH /api/v1/tickets/{id}/assign
- POST /api/v1/tickets/{id}/comments
- POST /api/v1/tickets/{id}/attachments
- GET /api/v1/reports/sla/compliance

## Teslim Notu

Detaylı analiz dokümanı deliverables klasöründe DOCX/PDF formatında saklanacaktır.
Bu md dosyası AI araçlarının projeyi daha iyi anlaması için kısa analiz özeti olarak kullanılacaktır.