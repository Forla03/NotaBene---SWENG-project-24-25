# === BUILD STAGE ===
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Imposta la directory di lavoro
WORKDIR /app

# Copia i file di configurazione Maven per sfruttare la cache Docker
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests -B

# === RUNTIME STAGE ===
FROM eclipse-temurin:17-jre

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Crea un utente non-root per sicurezza
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Imposta la directory di lavoro
WORKDIR /app

# Copia il JAR dall'immagine di build
COPY --from=build /app/target/*.jar app.jar

# Cambia proprietario dei file
RUN chown -R spring:spring /app

# Usa l'utente non-root
USER spring

# Espone la porta 8080
EXPOSE 8080

# Comando per avviare l'applicazione
ENTRYPOINT ["java", "-jar", "app.jar"]
