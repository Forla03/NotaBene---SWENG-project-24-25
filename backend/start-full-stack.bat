@echo off
echo === NotaBene Full Docker Stack ===
echo.

echo Avviando tutto lo stack con Docker Compose...
docker-compose up --build

echo.
echo Stack avviato! Servizi disponibili:
echo - PostgreSQL: localhost:5432
echo - Spring Boot: localhost:8080
echo.

pause
