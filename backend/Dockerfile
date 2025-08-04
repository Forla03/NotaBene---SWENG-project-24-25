# === BUILD STAGE ===
FROM maven:3.9.4-openjdk-17 AS build

# Imposta la directory di lavoro
WORKDIR /app

# Copia i file di configurazione Maven
COPY pom.xml .
COPY src ./src

# Costruisce l'applicazione
RUN mvn clean package -DskipTests

# === RUNTIME STAGE ===
FROM openjdk:17-jre-slim

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
