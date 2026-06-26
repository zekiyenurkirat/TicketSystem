# Keycloak Entegrasyonu

Bu proje, mevcut custom JWT + TOTP 2FA akışının yanına **yalnızca demo amaçlı** Keycloak JWT desteği ekler.

## Mimari

```
İstek gelir
  ├─ /api/v1/keycloak/**  →  KeycloakSecurityConfig (@Order=1)
  │                           Keycloak JWT (RSA imza), realm_access.roles
  │
  └─ diğer her şey       →  SecurityConfig (mevcut)
                             Custom HMAC-SHA256 JWT + TOTP 2FA akışı
```

Custom JWT akışı ve TOTP 2FA **değiştirilmemiştir**.

## Servisler

| Servis | URL | Açıklama |
|---|---|---|
| Keycloak Admin Console | http://localhost:8081 | admin / admin |
| Backend demo endpoint | http://localhost:8080/api/v1/keycloak/me | Keycloak JWT gerekir |

## Realm Bilgileri

- **Realm:** `ticketsystem`
- **Client:** `ticketsystem-frontend` (public, direct access grants enabled)

### Demo Kullanıcılar

| E-posta | Şifre | Rol |
|---|---|---|
| manager2026@example.com | Manager2026! | MANAGER |
| agent2026@example.com | Agent2026! | AGENT |
| customer2026@example.com | Customer2026! | CUSTOMER |

## Token Alma (curl)

```bash
curl -s -X POST http://localhost:8081/realms/ticketsystem/protocol/openid-connect/token \
  -d "client_id=ticketsystem-frontend" \
  -d "grant_type=password" \
  -d "username=manager2026@example.com" \
  -d "password=Manager2026!" \
  | jq -r .access_token
```

## Demo Endpoint

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/realms/ticketsystem/protocol/openid-connect/token \
  -d "client_id=ticketsystem-frontend" \
  -d "grant_type=password" \
  -d "username=manager2026@example.com" \
  -d "password=Manager2026!" \
  | jq -r .access_token)

curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/keycloak/me
```

Beklenen yanıt:
```json
{
  "sub": "...",
  "email": "manager2026@example.com",
  "preferred_username": "manager2026@example.com",
  "authorities": ["ROLE_MANAGER"]
}
```

## Teknik Notlar

### Issuer / JWK Hostname

Kullanıcı token'ı `http://localhost:8081` adresinden alır.
JWT'nin `iss` claim'i: `http://localhost:8081/realms/ticketsystem`

Backend, imzayı Docker iç ağı üzerinden doğrular:
`http://keycloak:8080/realms/ticketsystem/protocol/openid-connect/certs`

Proje `jwk-set-uri` kullanır (`issuer-uri` değil). Bu sayede:
- Issuer claim doğrulanmaz — yalnızca **RSA imzası** doğrulanır
- Keycloak kapalıyken backend **startup'ta crash olmaz** (lazy JWK fetch)
- Docker hostname / localhost mismatch sorunu yaşanmaz

Bu davranış **dev/demo ortamı** için kabul edilebilirdir.
Production'da `jwk-set-uri` + issuer doğrulaması birlikte yapılandırılmalıdır.

### Test Profili

`application-test.yaml` içinde `app.keycloak.enabled: false` değeri,
`KeycloakSecurityConfig` bean'inin `@ConditionalOnProperty` ile oluşturulmasını engeller.
Maven testleri Keycloak çalışmadan başarıyla geçer.

### Role Mapping

Keycloak JWT `realm_access.roles` claim'inden yalnızca şu roller alınır:
`CUSTOMER` → `ROLE_CUSTOMER`
`AGENT`    → `ROLE_AGENT`
`MANAGER`  → `ROLE_MANAGER`

`offline_access`, `default-roles-*` gibi Keycloak iç rolleri göz ardı edilir.
