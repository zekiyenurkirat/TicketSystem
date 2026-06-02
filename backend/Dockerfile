# ──────────────────────────────────────────────────────────────
# Stage 1: Build  —  Maven 3.9 + JDK 21
# ──────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# pom.xml önce kopyalanır; bağımlılıklar ayrı bir katmanda indirilir.
# Yalnızca src değiştiğinde bu katman cache'den gelir — build hızlanır.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Kaynak kod kopyalanır, testler atlanarak fat JAR üretilir
COPY src ./src
RUN mvn clean package -DskipTests -B

# ──────────────────────────────────────────────────────────────
# Stage 2: Runtime  —  küçük JRE 21 (Alpine)
# ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Fat JAR kopyalanır
COPY --from=build /build/target/*.jar app.jar

# Volume mount point: uploads dizini önceden oluşturulur
RUN mkdir -p uploads

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
