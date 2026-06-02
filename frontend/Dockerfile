# ──────────────────────────────────────────────────────────────
# Stage 1: Build  —  Node.js 22 (Alpine)
# ──────────────────────────────────────────────────────────────
FROM node:22-alpine AS build
WORKDIR /app

# package.json ve lock dosyası önce kopyalanır; bağımlılıklar ayrı
# bir katmanda yüklenir. Yalnızca kaynak değişince cache'den gelir.
COPY package.json package-lock.json ./
RUN npm ci

# Kaynak kod kopyalanır, production build üretilir (dist/)
COPY . .
RUN npm run build

# ──────────────────────────────────────────────────────────────
# Stage 2: Runtime  —  Nginx (Alpine)
# ──────────────────────────────────────────────────────────────
FROM nginx:1.27-alpine AS runtime

# Production build kopyalanır
COPY --from=build /app/dist /usr/share/nginx/html

# Özel Nginx konfigürasyonu: SPA routing + /api proxy
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
