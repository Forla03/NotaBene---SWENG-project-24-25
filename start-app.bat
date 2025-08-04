@echo off
echo Starting NotaBene Backend...
echo.
echo Make sure you have created the .env file in the backend directory!
echo.
echo This will start:
echo - PostgreSQL database
echo - Spring Boot backend
echo.
echo After this completes, start the frontend with:
echo   cd frontend
echo   npm install
echo   npm start
echo.
cd backend
docker compose up --build
