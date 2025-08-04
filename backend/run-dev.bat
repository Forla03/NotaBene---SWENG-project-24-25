@echo off
echo === NotaBene Backend Setup ===
echo.

echo Impostando variabili d'ambiente per sviluppo locale...
set POSTGRES_DB=notabenedb
set POSTGRES_USER=fraforla
set POSTGRES_PASSWORD=Francesco2003
set SPRING_PROFILES_ACTIVE=dev

echo.
echo Variabili d'ambiente impostate:
echo - POSTGRES_DB: %POSTGRES_DB%
echo - POSTGRES_USER: %POSTGRES_USER%
echo - POSTGRES_PASSWORD: [HIDDEN]
echo - SPRING_PROFILES_ACTIVE: %SPRING_PROFILES_ACTIVE%
echo.

echo Avviando l'applicazione Spring Boot...
mvn spring-boot:run

pause
