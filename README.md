# TicketSystem

**IT Servis Yönetim Sistemi** | Java 21 · Spring Boot 3.5.14 · React · PostgreSQL · Docker

TicketSystem, kurumsal IT destek süreçlerini tek bir platformda merkezileştiren, rol bazlı erişim kontrolü ve SLA takibi ile desteklenen bir destek talebi yönetim uygulamasıdır.

Dağınık e-posta ve telefon tabanlı destek akışlarının yol açtığı takip eksiklikleri, SLA ihlalleri ve hesap verebilirlik sorunlarını ortadan kaldırmak için geliştirilmiştir. Her talep oluşturulduğu andan kapandığı ana kadar izlenebilir; her işlem kayıt altına alınır.

---

## Özellikler

- JWT kimlik doğrulama (HMAC-SHA256) ve BCrypt şifre güvenliği
- TOTP tabanlı iki aşamalı doğrulama (2FA) — authenticator uygulaması ile
- Keycloak entegrasyonu — `/api/v1/keycloak/**` altında RS256 JWT desteği (demo)
- Rol bazlı yetkilendirme: CUSTOMER, AGENT, MANAGER
- URL düzeyi ve servis düzeyi (ownership) iki katmanlı erişim kontrolü
- AGENT yetki kısıtı: yorum, dosya, statü ve öncelik işlemleri yalnızca atanmış ticketlarda
- Ticket oluşturma, listeleme, detay görüntüleme ve atama
- Ticket yaşam döngüsü yönetimi — 7 statü: NEW → ASSIGNED → IN_PROGRESS → WAITING_FOR_CUSTOMER → RESOLVED → CLOSED / CANCELLED
- Öncelik triage/review sistemi: impact × urgency matrisi ile suggestedPriority hesaplama
- SLA otomasyonu: önceliğe göre dueDate hesaplama ve her 5 dakikada bir çalışan eskalasyon motoru
- INTERNAL / EXTERNAL yorum sistemi
- Güvenli dosya upload/download (MIME whitelist, 10 MB limit, path traversal engeli)
- Otomatik bildirim sistemi (SLA ihlali, yaklaşma, atama, kayıt, eskalasyon)
- Agent atama talebi akışı (AGENT → MANAGER onayı)
- Müşteri kayıt talebi akışı (manager onaylı hesap oluşturma)
- Rol bazlı dashboard (Customer / Agent / Manager)
- Kullanıcı yönetimi (oluşturma, pasife alma, rol bazlı listeleme)
- Karanlık / Aydınlık tema desteği
- jBPM 7 ile BPMN 2.0 tabanlı ticket iş akışı
- Apache Kafka ticket event pipeline (CREATE / ASSIGN / STATUS_CHANGE)
- OpenSearch indexleme ve OpenSearch Dashboards ile arama
- Prometheus + Grafana ile metrik tabanlı monitoring
- OpenTelemetry + Jaeger ile dağıtık izleme altyapısı
- JUnit 5 + Mockito ile birim test kalite güvencesi
- Swagger / OpenAPI dokümantasyonu

---

## Kullanıcı Rolleri

| Rol | Açıklama |
|---|---|
| **CUSTOMER** | Destek talebi açar, kendi taleplerini takip eder, yorum ve dosya ekler |
| **AGENT** | Tüm talepleri görüntüler; yalnızca kendisine atanmış taleplerde statü günceller, iç/dış yorum ekler, dosya yükler; atama talebi oluşturur |
| **MANAGER** | Ticket atar, önceliği triage eder, kullanıcıları yönetir, sistem genelini izler |

---

## Teknoloji Yığını

### Backend

| Teknoloji | Sürüm / Açıklama |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.14 |
| Spring Security | JWT tabanlı stateless kimlik doğrulama |
| Spring Data JPA / Hibernate | ORM katmanı |
| Flyway | Veritabanı migration yönetimi |
| jBPM | 7.74.1 — BPMN 2.0 iş akışı motoru |
| Spring Kafka | Ticket event producer/consumer |
| Log4j2 | Rolling file loglama |
| Spring Boot Actuator | Sağlık ve metrik endpoint'leri |
| Micrometer | Prometheus metrik registry |
| Springdoc OpenAPI | Swagger UI dokümantasyonu |
| JJWT | 0.12.6 — JWT üretme ve doğrulama |
| Spring Security OAuth2 Resource Server | Keycloak RS256 JWT doğrulaması (demo) |
| Lombok | Tekrarlayan kod azaltma |

### Frontend

| Teknoloji | Sürüm / Açıklama |
|---|---|
| React | 18 |
| TypeScript | Tip güvenli geliştirme |
| Vite | Hızlı geliştirme ve production build |
| Tailwind CSS | Utility-first stil |
| Axios | HTTP istemcisi |
| React Router | v6 — client-side yönlendirme |
| Nginx | SPA servis ve /api/ proxy |

### Veritabanı

| Teknoloji | Açıklama |
|---|---|
| PostgreSQL | 16 — kalıcı veri depolama |

### DevOps / Observability

| Teknoloji | Açıklama |
|---|---|
| Docker Compose | 11 servis orkestrasyonu |
| Apache Kafka | 3.7.0 KRaft modunda event streaming |
| OpenSearch | 2.13.0 — ticket event indexleme |
| OpenSearch Dashboards | Arama ve görselleştirme |
| Prometheus | 2.51.2 — metrik toplama |
| Grafana | 10.4.2 — metrik görselleştirme |
| OpenTelemetry Collector | Trace toplama ve iletme |
| Jaeger | 1.57 — dağıtık izleme UI |
| Keycloak | 24.0.4 — harici kimlik sağlayıcı (demo) |

### Test ve Kalite Güvence

| Teknoloji | Açıklama |
|---|---|
| JUnit 5 | Birim test çerçevesi |
| Mockito | Mock tabanlı bağımlılık soyutlama |
| Spring Boot Test | Entegrasyon test altyapısı |

---

## Sistem Mimarisi

TicketSystem, her bileşenin Docker Compose ile orkestrasyona alındığı katmanlı bir mimariye sahiptir.

```
[Kullanıcı Tarayıcısı]
        │
[React SPA — Nginx  :3000]
        │  /api/ proxy
[Spring Boot REST API  :8080]
        │
   ┌────┴──────────────────────────────────────────┐
   │               │               │               │
[PostgreSQL]  [Apache Kafka]  [Actuator +]  [OpenTelemetry]
              [:9092]         [Micrometer]  [Distributed
[Flyway]           │               │          Tracing]
[Migration]   [OpenSearch]   [Prometheus]
              [Dashboards]    [Grafana]
              [:9200/:5601]  [:9090/:3001]
```

**Olay akışı:** Ticket operasyonlarında (oluşturma, atama, statü değişikliği) Kafka `ticket-events` topic'ine event gönderilir. Consumer bu event'leri OpenSearch'e indexler.

**Monitoring akışı:** Prometheus, `backend:8080/actuator/prometheus` endpoint'ini 15 saniye aralıklarla scrape eder. Grafana bu veriyi JVM, HTTP ve sistem metrikleri olarak görselleştirir.

**Log akışı:** Log4j2 logları hem konsola hem `logs/app.log` dosyasına yazar. Günlük ve boyut bazlı rotate; 7 gün arşiv.

**Observability:** OpenTelemetry, uygulama içi ve servisler arası işlemleri uçtan uca trace eder; gecikme kaynakları ve hata noktaları span bazlı tespit edilir.

---

## Port Listesi

| Servis | Adres |
|---|---|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| Backend Health | http://localhost:8080/actuator/health |
| OpenSearch | http://localhost:9200 |
| OpenSearch Dashboards | http://localhost:5601 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3001 |
| Keycloak Admin Console | http://localhost:8081 |
| Jaeger UI | http://localhost:16686 |

---

## Kurulum ve Çalıştırma

### Ön Koşullar

- [Docker Desktop](https://www.docker.com/products/docker-desktop) — yüklü ve çalışır durumda
- Git

### 1. Repository'yi Klonlayın

```bash
git clone https://github.com/zekiyenurkirat/TicketSystem.git
cd TicketSystem
```

### 2. Ortam Değişkenlerini Ayarlayın

```bash
# Windows
copy .env.example .env

# macOS / Linux
cp .env.example .env
```

`.env` dosyasını açarak değerleri düzenleyin:

```
DB_USERNAME=postgres
DB_PASSWORD=güçlü-bir-şifre
JWT_SECRET=en-az-32-karakter-rastgele-anahtar
```

JWT anahtarı için rastgele değer üretmek üzere şu komut kullanılabilir:

```bash
openssl rand -hex 32
```

### 3. Docker Build

```bash
docker compose build
```

### 4. Servisleri Başlatın

```bash
docker compose up -d
```

### 5. Servis Durumunu Kontrol Edin

```bash
docker compose ps
```

Tüm servislerin `running` durumunda görünmesi beklenir: `postgres`, `kafka`, `opensearch`, `opensearch-dashboards`, `otel-collector`, `jaeger`, `backend`, `frontend`, `keycloak`, `prometheus`, `grafana`

> **Not:** Backend, `postgres`, `kafka` ve `opensearch` servislerinin sağlık kontrollerini geçmesini bekler. İlk başlatmada OpenSearch hazır olana kadar 30–60 saniye sürebilir.

---

## Demo Kullanıcı

Sistem boş veritabanında başlatıldığında `DataSeeder` otomatik olarak bir MANAGER hesabı oluşturur. Sistemde zaten MANAGER varsa seed adımı atlanır; mevcut veri etkilenmez.

| Alan | Değer |
|---|---|
| E-posta | manager2026@example.com |
| Şifre | Sifre1234 |
| Rol | MANAGER |

Bu hesapla giriş yaptıktan sonra AGENT ve CUSTOMER hesapları arayüzdeki Kullanıcı Yönetimi bölümünden ya da `POST /api/v1/users/admin` endpoint'i üzerinden oluşturulabilir.

---

## Doğrulama Komutları

Sistem ayağa kalktıktan sonra aşağıdaki komutlarla bileşenler doğrulanabilir.

**Backend sağlık kontrolü:**

```bash
curl.exe "http://localhost:8080/actuator/health"
```

**Prometheus metrik endpoint:**

```bash
curl.exe "http://localhost:8080/actuator/prometheus"
```

**Prometheus scrape durumu:**

```bash
curl.exe "http://localhost:9090/api/v1/query?query=up"
```

**OpenSearch index listesi:**

```bash
curl.exe "http://localhost:9200/_cat/indices?v"
```

**OpenSearch ticket event arama:**

```bash
curl.exe "http://localhost:9200/ticket-events/_search?pretty"
```

**Kafka event tüketimi:**

```bash
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic ticket-events \
  --from-beginning \
  --timeout-ms 5000
```

---

## Demo Akışı

1. `http://localhost:3000` adresinde Manager hesabıyla giriş yapın
2. Dashboard'da sistem geneli özeti görüntüleyin
3. "Yeni Talep" formuyla bir ticket oluşturun — priority, impact ve urgency seçin; sistem suggestedPriority ve dueDate hesaplar
4. Tickets sayfasından ticket'ı bir AGENT'a atayın
5. AGENT hesabıyla giriş yapın; atanan ticket'ı IN_PROGRESS statüsüne alın
6. Ticket'a EXTERNAL ve INTERNAL yorum ekleyin
7. Ticket'a dosya yükleyin ve indirin
8. Kafka consumer komutuyla `ticket-events` topic'indeki CREATE / ASSIGN / STATUS_CHANGE event'lerini görüntüleyin
9. `http://localhost:9200/ticket-events/_search?pretty` ile OpenSearch'te indexlenen event'leri kontrol edin
10. `http://localhost:9090/targets` adresinde `ticketsystem-backend` target'ının UP durumunu doğrulayın
11. `http://localhost:3001` adresinde Grafana'da Prometheus datasource bağlantısını kurun ve JVM / HTTP metriklerini görüntüleyin

---

## Güvenlik

- **JWT Authentication:** Stateless HMAC-SHA256 token tabanlı kimlik doğrulama; 24 saat geçerlilik süresi. İmzalama anahtarı environment variable'dan okunur; kod içine gömülmez.
- **TOTP 2FA:** Giriş sonrası authenticator uygulamasından alınan tek kullanımlık kod ile ikinci doğrulama adımı.
- **Keycloak (Demo):** `/api/v1/keycloak/**` altında RS256 JWT doğrulaması. Mevcut custom JWT + TOTP akışı değiştirilmemiştir.
- **BCrypt:** Tüm şifreler BCrypt ile hashlenerek saklanır; düz metin hiçbir zaman veritabanında tutulmaz.
- **İki Katmanlı Yetkilendirme:** `SecurityFilterChain` ile URL bazlı rol kuralları; servis katmanında `SecurityContextHolder` üzerinden ownership ve assignment kontrolleri.
- **AGENT Yetki Kısıtı:** Yorum, dosya yükleme, statü değişikliği ve öncelik incelemesi yalnızca AGENT'a atanmış ticketlarda gerçekleştirilebilir; backend servis katmanında zorunlu kılınmıştır.
- **Dosya Upload Güvenliği:** MIME type ve uzantı whitelist kontrolü (PDF, DOC, DOCX, PNG, JPG, TXT), 10 MB boyut sınırı, path traversal engeli. Fiziksel depolama yolu hiçbir API yanıtında döndürülmez.
- **Actuator Güvenliği:** Yalnızca `/actuator/health` ve `/actuator/prometheus` endpoint'leri public erişime açıktır; diğer actuator endpoint'leri kimlik doğrulama gerektirir.

---

## Proje Yapısı

```
TicketSystem/
├── backend/                    # Spring Boot uygulaması
│   ├── src/main/java/          # Kaynak kod
│   ├── src/main/resources/     # Konfigürasyon, migration, BPMN
│   └── Dockerfile              # Multi-stage build (Maven + JRE Alpine)
├── frontend/                   # React SPA
│   ├── src/                    # Kaynak kod
│   ├── nginx.conf              # SPA routing + /api/ proxy
│   └── Dockerfile              # Multi-stage build (Node + Nginx Alpine)
├── keycloak/
│   └── realm-export.json       # Keycloak realm + demo kullanıcı import tanımı
├── monitoring/
│   ├── prometheus.yml          # Prometheus scrape konfigürasyonu
│   └── otel-collector-config.yaml  # OTel Collector → Jaeger pipeline
├── docs/                       # Proje dokümantasyonu
│   ├── proje-gereksinimleri.md
│   ├── teknik-analiz.md
│   ├── security-authorization.md
│   ├── keycloak.md             # Keycloak entegrasyonu ve demo komutları
│   └── roadmap.md
├── docker-compose.yml          # 11 servis orkestrasyonu
└── .env.example                # Ortam değişkeni şablonu
```

---

## Dokümantasyon

| Doküman | Açıklama |
|---|---|
| [Proje Gereksinimleri](docs/proje-gereksinimleri.md) | Fonksiyonel ve teknik gereksinimler |
| [Teknik Analiz](docs/teknik-analiz.md) | Mimari yapı ve teknoloji kararları |
| [Güvenlik & Yetkilendirme](docs/security-authorization.md) | JWT, TOTP 2FA, rol bazlı erişim, ownership ve assignment kuralları |
| [Keycloak Entegrasyonu](docs/keycloak.md) | Keycloak demo kurulumu, token alma, role mapping |
| [Roadmap](docs/roadmap.md) | Faz bazlı geliştirme planı |

---

## GitHub

[https://github.com/zekiyenurkirat/TicketSystem](https://github.com/zekiyenurkirat/TicketSystem)
