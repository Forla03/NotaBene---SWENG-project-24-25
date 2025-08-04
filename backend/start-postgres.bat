@echo off
echo === NotaBene Docker Setup ===
echo.

echo Avviando PostgreSQL con Docker Compose...
docker-compose up -d postgres

echo.
echo Attendendo che PostgreSQL sia pronto...
timeout /t 10

echo.
echo PostgreSQL è pronto! Connessione disponibile su:
echo - Host: localhost
echo - Port: 5432
echo - Database: %POSTGRES_DB%
echo - Username: %POSTGRES_USER%
echo.

echo Ora puoi avviare Spring Boot con:
echo mvn spring-boot:run -Dspring-boot.run.profiles=dev
echo.
echo Oppure per vedere i logs di PostgreSQL:
echo docker-compose logs -f postgres

pause
