# NotaBene — SWENG Project 2024/25

**NotaBene** is a collaborative web application that allows users to share short text notes (maximum 280 characters) with other users. This full-stack project consists of a Spring Boot backend and a React TypeScript frontend.

## 🏗️ Project Architecture

- **Backend**: Spring Boot 3.x with Java 21
- **Frontend**: React 18+ with TypeScript
- **Database**: PostgreSQL (via Docker)
- **API**: RESTful services
- **Build Tools**: Maven (backend), npm (frontend)

## 📋 Requirements

- **Java 17+** (tested with Java 17)
- **Node.js 16+** & npm
- **Docker & Docker Compose**
- **Maven 3.6+**
- **PostgreSQL** (via Docker)

## 🚀 Quick Start

### 1. Clone and Setup

```bash
git clone <repository-url>
cd NotaBene---SWENG-project-24-25
```

### 2. Backend Setup

#### Required Local Files (Not Tracked by Git)

**⚠️ IMPORTANT**: You must create these files locally as they contain sensitive information and are not committed to the repository.

1. **Create environment file** - `backend/.env`
   
   Create a `.env` file in the `backend/` folder with your database credentials:

   ```env
   # === DOCKER COMPOSE ENVIRONMENT ===
   # Database configuration
   POSTGRES_DB=notabenedb
   POSTGRES_USER=your_username
   POSTGRES_PASSWORD=your_secure_password
   
   # Ports
   DB_PORT=5432
   APP_PORT=8080
   
   # Spring profiles
   SPRING_PROFILES_ACTIVE=dev
   
   # Database connection (for direct host connection)
   DB_HOST=localhost
   ```

#### Database Configuration

1. **Start PostgreSQL with Docker**
   ```bash
   cd backend
   docker compose up -d postgres
   ```
   
   Or use the provided script:
   ```bash
   cd backend
   .\start-postgres.bat
   ```

2. **Run the Spring Boot application**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```
   
   Or use the provided script:
   ```bash
   cd backend
   .\run-dev.bat
   ```
   
   The backend will be available at `http://localhost:8080`

#### Alternative: Full Docker Stack

If you prefer to run everything in Docker (optional):

1. **Uncomment the app service** in `docker-compose.yml`
2. **Build and run the complete stack**
   ```bash
   cd backend
   docker compose up --build
   ```
   
   Or use the provided script:
   ```bash
   cd backend
   .\start-full-stack.bat
   ```

### 3. Frontend Setup

```bash
cd frontend
npm install
npm start
```

The frontend will be available at `http://localhost:3000`

## 📁 Project Structure

```
NotaBene---SWENG-project-24-25/
├── README.md
├── .gitignore
├── backend/
│   ├── docker-compose.yml          # Docker configuration
│   ├── Dockerfile                  # Docker image for Spring Boot
│   ├── pom.xml                     # Maven configuration
│   ├── .env                        # ⚠️ CREATE LOCALLY - Environment variables
│   ├── .gitignore                  # Git ignore rules
│   ├── start-postgres.bat          # Script to start only PostgreSQL
│   ├── start-full-stack.bat        # Script to start full Docker stack
│   ├── run-dev.bat                 # Script for local development
│   └── src/
│       ├── main/
│       │   ├── java/com/example/myspringapp/
│       │   └── resources/
│       │       ├── application.properties          # Main configuration
│       │       ├── application-dev.properties      # Development config
│       │       ├── application-docker.properties   # Docker config
│       │       ├── application-prod.properties     # Production config
│       │       └── application-local.properties    # ⚠️ CREATE LOCALLY if needed
│       └── test/
└── frontend/
    ├── package.json
    ├── .env                        # ⚠️ CREATE LOCALLY - Frontend environment
    ├── public/
    └── src/
        ├── components/NotesList.tsx
        ├── services/api.ts
        ├── App.tsx
        └── App.css
```

### Files to Create Locally

The following files are **not tracked by Git** and must be created manually:

- `backend/.env` - **Required** for database credentials
- `backend/src/main/resources/application-local.properties` - Optional for local overrides
- `frontend/.env` - **Required** for frontend configuration

## 🛠️ Available Scripts

### Backend (Maven)

```bash
cd backend

# Development (recommended)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or use provided scripts
.\run-dev.bat              # Start with environment setup
.\start-postgres.bat       # Start only PostgreSQL in Docker
.\start-full-stack.bat     # Start everything in Docker

# Standard Maven commands
mvn test                   # Run tests
mvn clean package          # Build the project
```

### Frontend (npm)

```bash
cd frontend
npm start                 # Run in development mode
npm test                  # Launch test runner
npm run build            # Build for production
npm run eject            # Eject from Create React App (⚠️ irreversible)
```

## 🌐 API Endpoints

The backend exposes RESTful APIs at `http://localhost:8080/api`:

- `GET /api/notes` - Get all notes
- `POST /api/notes` - Create a new note
- `GET /api/notes/{id}` - Get a specific note
- `PUT /api/notes/{id}` - Update a note
- `DELETE /api/notes/{id}` - Delete a note

## ✨ Features

### Frontend Features
- **Modern UI**: Clean, Twitter-like interface for sharing notes
- **Real-time Updates**: Automatically refreshes notes list
- **Character Limit**: Enforces 280 character limit for notes
- **Responsive Design**: Works on desktop and mobile devices
- **TypeScript**: Full type safety and better development experience
- **Form Validation**: Client-side validation for user inputs

### Backend Features
- **RESTful API**: Clean API design following REST principles
- **Database Integration**: PostgreSQL with JPA/Hibernate
- **Environment Configuration**: Secure configuration via environment variables and profiles
- **CORS Support**: Configured for frontend-backend communication
- **Docker Support**: Full containerization with Docker Compose
- **Multiple Profiles**: Development, Docker, and Production configurations
- **Security**: Environment variables for sensitive data, proper .gitignore setup

## 🔧 Development

### Docker Compose Configuration

The `docker-compose.yml` file includes:

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16
    container_name: notabene-postgres
    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "${DB_PORT:-5432}:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER} -d ${POSTGRES_DB}"]
      interval: 30s
      timeout: 10s
      retries: 3
    networks:
      - notabene-network

  # Uncomment to run Spring Boot in Docker too
  # app:
  #   build: .
  #   container_name: notabene-app
  #   environment:
  #     SPRING_PROFILES_ACTIVE: docker
  #     DB_HOST: postgres
  #     # ... other environment variables
  #   ports:
  #     - "${APP_PORT:-8080}:8080"
  #   depends_on:
  #     postgres:
  #       condition: service_healthy
  #   networks:
  #     - notabene-network

volumes:
  pgdata:

networks:
  notabene-network:
    driver: bridge
```

### Environment Variables

**Backend (.env):** ⚠️ **Must be created locally**
```env
# === DOCKER COMPOSE ENVIRONMENT ===
POSTGRES_DB=notabenedb
POSTGRES_USER=your_username
POSTGRES_PASSWORD=your_secure_password

# Ports
DB_PORT=5432
APP_PORT=8080

# Spring profiles
SPRING_PROFILES_ACTIVE=dev

# Database connection
DB_HOST=localhost
```

**Frontend (.env):** ⚠️ **Must be created locally**
```env
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_NAME=NotaBene Frontend
```

### Spring Boot Profiles

The application supports multiple profiles:

- **`dev`** - Development mode (recommended for local development)
- **`docker`** - When running Spring Boot in Docker container
- **`prod`** - Production configuration
- **`local`** - Local overrides (if `application-local.properties` exists)

## 🚦 Running the Complete Application

### Method 1: Hybrid Setup (Recommended for Development)

1. **Create the required `.env` file**
   ```bash
   cd backend
   # Create .env file with your database credentials (see example above)
   ```

2. **Start PostgreSQL in Docker**
   ```bash
   cd backend
   docker compose up -d postgres
   # Or use: .\start-postgres.bat
   ```

3. **Start the backend locally**
   ```bash
   cd backend
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   # Or use: .\run-dev.bat
   ```

4. **Start the frontend**
   ```bash
   cd frontend
   npm install
   npm start
   ```

### Method 2: Full Docker Stack

1. **Create the required `.env` file** (same as above)

2. **Uncomment the `app` service** in `docker-compose.yml`

3. **Start everything in Docker**
   ```bash
   cd backend
   docker compose up --build
   # Or use: .\start-full-stack.bat
   ```

4. **Start the frontend** (still runs locally)
   ```bash
   cd frontend
   npm start
   ```

### Access Points

- **Frontend**: `http://localhost:3000`
- **Backend API**: `http://localhost:8080/api`
- **PostgreSQL**: `localhost:5432` (when using Docker)

## 📝 Notes

- **Security**: Sensitive files (`.env`, `application-local.properties`) are not tracked by Git
- **Profiles**: The application uses Spring Boot profiles for different environments
- **Docker**: You can run just PostgreSQL in Docker (recommended) or the full stack
- **Development**: The hybrid approach (DB in Docker, app locally) is fastest for development
- **Scripts**: Use the provided `.bat` scripts on Windows for easier setup
- **Database**: Data persists in Docker volumes between container restarts
- **CORS**: The application is configured to allow frontend-backend communication

## 🔧 Troubleshooting

### Common Issues

1. **"Cannot connect to database"**
   - Ensure PostgreSQL container is running: `docker compose ps`
   - Check `.env` file exists and has correct credentials
   - Verify database is ready: `docker compose logs postgres`

2. **"Environment variables not found"**
   - Create the `.env` file in the `backend/` directory
   - Ensure `.env` file has no spaces around the `=` signs
   - Check file encoding (should be UTF-8)

3. **"Port already in use"**
   - Change ports in `.env` file (`DB_PORT=5433`, `APP_PORT=8081`)
   - Or stop conflicting services

4. **"Maven build fails"**
   - Ensure Java 17+ is installed: `java -version`
   - Clean and rebuild: `mvn clean install`

5. **"Spring Boot profile not working"**
   - Set profile explicitly: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`
   - Check `application-{profile}.properties` files exist

## 🏷️ Learn More

- [React Documentation](https://reactjs.org/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Create React App Documentation](https://facebook.github.io/create-react-app/docs/getting-started)
- [TypeScript Documentation](https://www.typescriptlang.org/)

---
