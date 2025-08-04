# NotaBene — SWENG Project 2024/25

**NotaBene** is a collaborative web application that allows users to share short text notes (maximum 280 characters) with other users. This full-stack project consists of a Spring Boot backend (running in Docker) and a React TypeScript frontend (running locally with npm).

## 🏗️ Project Architecture

- **Backend**: Spring Boot 3.x with Java 21 (Docker)
- **Database**: PostgreSQL 16 (Docker)
- **Frontend**: React 18+ with TypeScript (Local development server)
- **API**: RESTful services
- **Containerization**: Docker & Docker Compose (backend only)

## 📋 Requirements

- **Docker Desktop** (includes Docker Compose) - for backend
- **Node.js 16+** & npm - for frontend
- **Git** (to clone the repository)

## ⚠️ Important: Local Configuration Files

**Before starting the application, you must create local configuration files that are NOT tracked by Git due to sensitive information:**

### 1. Backend Environment File (Required)

Create a file named `.env` in the `backend/` directory:

**File: `backend/.env`**
```env
# Database Configuration
POSTGRES_DB=notabenedb
POSTGRES_USER=notabene_user
POSTGRES_PASSWORD=your_secure_password_here

# Ports
DB_PORT=5432
APP_PORT=8080
```

> **Note**: Replace `your_secure_password_here` with a strong password of your choice.

### 2. Frontend Environment File (Already Created)

The file `frontend/.env` is already configured for local development:

**File: `frontend/.env`**
```env
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_NAME=NotaBene Frontend
```

## 🚀 Quick Start

### Step 1: Clone the Repository

```bash
git clone <repository-url>
cd NotaBene---SWENG-project-24-25
```

### Step 2: Create the Backend Configuration File

Create the `backend/.env` file as described above. **The backend will not start without this file.**

**Example `backend/.env`:**
```env
# Database Configuration
POSTGRES_DB=notabenedb
POSTGRES_USER=notabene_user
POSTGRES_PASSWORD=MySecurePassword123!

# Ports
DB_PORT=5432
APP_PORT=8080
```

### Step 3: Start the Backend (Docker)

#### Option A: Using the Convenience Script (Windows)
```powershell
.\start-app.bat
```

#### Option B: Using Docker Compose Directly
```bash
cd backend
docker compose up --build
```

Wait for the backend to be ready (you'll see "Started MySpringApp" in the logs).

### Step 4: Start the Frontend (Local)

Open a **new terminal** and run:

```bash
cd frontend
npm install
npm start
```

### Step 5: Access the Application

- **Frontend (Main Application)**: http://localhost:3000
- **Backend API**: http://localhost:8080/api  
- **Backend Health Check**: http://localhost:8080/actuator/health

## 🔧 Development Workflow

### Starting Development

1. **Terminal 1 - Backend**:
   ```bash
   cd backend
   docker compose up --build
   ```

2. **Terminal 2 - Frontend**:
   ```bash
   cd frontend
   npm install
   npm start
   ```

### Stopping the Application

- **Frontend**: `Ctrl+C` in the frontend terminal
- **Backend**: `Ctrl+C` in the backend terminal, then `docker compose down`

## 📁 Project Structure

```
NotaBene---SWENG-project-24-25/
├── README.md
├── start-app.bat                   # Windows script to start backend
├── backend/
│   ├── docker-compose.yml          # Docker configuration (backend only)
│   ├── Dockerfile                  # Docker image for Spring Boot
│   ├── .env                        # ⚠️ CREATE LOCALLY - Environment variables
│   ├── pom.xml                     # Maven configuration
│   ├── init.sql/                   # Database initialization scripts
│   └── src/
│       ├── main/
│       │   ├── java/com/example/myspringapp/
│       │   └── resources/
│       │       ├── application.properties          # Main configuration
│       │       ├── application-docker.properties   # Docker config
│       │       └── application-prod.properties     # Production config
│       └── test/
└── frontend/
    ├── package.json               # npm configuration
    ├── .env                       # ⚠️ Frontend environment (already created)
    ├── public/
    └── src/
        ├── components/NotesList.tsx
        ├── services/api.ts
        ├── App.tsx
        └── App.css
```

### Files You Must Create Locally

This file is **NOT tracked by Git** for security reasons and must be created manually:

1. **`backend/.env`** - **REQUIRED** - Database credentials and port configuration

### Pre-configured Files

These files are already set up for local development:

1. **`frontend/.env`** - Frontend API configuration (already exists)
2. **`frontend/package.json`** - npm dependencies and scripts

## 🛠️ Available Commands

### Backend (Docker Commands)

```bash
# Start the backend and database
cd backend
docker compose up --build

# Start in detached mode (background)
docker compose up -d --build

# Stop the backend
docker compose down

# View logs
docker compose logs              # All services
docker compose logs postgres     # Database logs
docker compose logs app         # Backend logs  

# Restart the backend
docker compose restart app

# Rebuild without cache
docker compose build --no-cache

# Remove all containers and volumes (⚠️ This will delete all data)
docker compose down -v
```

### Frontend (npm Commands)

```bash
cd frontend
npm install               # Install dependencies
npm start                 # Start development server (http://localhost:3000)
npm test                  # Run tests
npm run build            # Build for production
npm run eject            # Eject from Create React App (⚠️ irreversible)
```

### Windows Convenience Script

```powershell
# Start backend only (equivalent to: cd backend && docker compose up --build)
.\start-app.bat
```

## 🌐 API Endpoints

The backend exposes RESTful APIs at `http://localhost:8080/api/`:

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/notes` | Get all notes |
| POST | `/api/notes` | Create a new note |
| GET | `/api/notes/{id}` | Get a specific note |
| PUT | `/api/notes/{id}` | Update a note |
| DELETE | `/api/notes/{id}` | Delete a note |
| GET | `/actuator/health` | Application health check |

## 🔧 Application Configuration

### Environment Variables Reference

#### Backend (.env file)
```env
# Database Configuration
POSTGRES_DB=notabenedb              # Database name
POSTGRES_USER=notabene_user         # Database username  
POSTGRES_PASSWORD=your_password     # Database password (CHANGE THIS!)

# Port Configuration
DB_PORT=5432                        # PostgreSQL port
APP_PORT=8080                       # Spring Boot application port
```

#### Frontend (.env file - already created)
```env
REACT_APP_API_URL=http://localhost:8080/api    # Backend API URL
REACT_APP_NAME=NotaBene Frontend               # Application name
```

### Docker Compose Services (Backend Only)

The application consists of two Docker services:

1. **`postgres`** - PostgreSQL 16 database
   - Port: 5432 (configurable via `DB_PORT`)
   - Data persistence via Docker volume
   - Automatic initialization with `init.sql`

2. **`app`** - Spring Boot backend
   - Port: 8080 (configurable via `APP_PORT`)
   - Connects to PostgreSQL database
   - Health checks and automatic restart

The frontend runs locally using `npm start` and connects to the backend via HTTP.

## ✨ Features

### Frontend Features
- **Modern UI**: Clean, Twitter-like interface for sharing notes
- **Real-time Updates**: Automatically refreshes notes list
- **Character Limit**: Enforces 280 character limit for notes
- **Responsive Design**: Works on desktop and mobile devices
- **TypeScript**: Full type safety and better development experience
- **Hot Reload**: Fast development with automatic browser refresh

### Backend Features
- **RESTful API**: Clean API design following REST principles
- **Database Integration**: PostgreSQL with JPA/Hibernate
- **Environment Configuration**: Secure configuration via environment variables
- **CORS Support**: Configured for frontend-backend communication
- **Docker Support**: Containerized backend and database
- **Health Checks**: Built-in health monitoring endpoints
- **Security**: Environment variables for sensitive data, proper .gitignore setup

### Infrastructure Features
- **Hybrid Architecture**: Docker backend + local frontend development
- **Automatic Database Setup**: Database schema initialized automatically
- **Service Health Checks**: Ensures backend starts after database
- **Volume Persistence**: Database data survives container restarts
- **Development Optimized**: Fast frontend development with hot reload

## 🌐 API Endpoints

The backend exposes RESTful APIs at `http://localhost:8080/api/`:

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/notes` | Get all notes |
| POST | `/api/notes` | Create a new note |
| GET | `/api/notes/{id}` | Get a specific note |
| PUT | `/api/notes/{id}` | Update a note |
| DELETE | `/api/notes/{id}` | Delete a note |
| GET | `/actuator/health` | Application health check |

## 🔧 Application Configuration

### Environment Variables Reference

#### Backend (.env file)
```env
# Database Configuration
POSTGRES_DB=notabenedb              # Database name
POSTGRES_USER=notabene_user         # Database username  
POSTGRES_PASSWORD=your_password     # Database password (CHANGE THIS!)

# Port Configuration
DB_PORT=5432                        # PostgreSQL port
APP_PORT=8080                       # Spring Boot application port
FRONTEND_PORT=3000                  # Frontend port (Nginx)
```

### Docker Compose Services

The application consists of three Docker services:

1. **`postgres`** - PostgreSQL 16 database
   - Port: 5432 (configurable via `DB_PORT`)
   - Data persistence via Docker volume
   - Automatic initialization with `init.sql`

2. **`app`** - Spring Boot backend
   - Port: 8080 (configurable via `APP_PORT`)
   - Connects to PostgreSQL database
   - Health checks and automatic restart

3. **`frontend`** - React + Nginx frontend
   - Port: 3000 (configurable via `FRONTEND_PORT`)
   - Serves static React build
   - Proxies API calls to backend
   - Handles React Router routing

## ✨ Features

### Frontend Features
- **Modern UI**: Clean, Twitter-like interface for sharing notes
- **Real-time Updates**: Automatically refreshes notes list
- **Character Limit**: Enforces 280 character limit for notes
- **Responsive Design**: Works on desktop and mobile devices
- **TypeScript**: Full type safety and better development experience
- **Containerized**: Runs in Docker with Nginx for production-like setup

### Backend Features
- **RESTful API**: Clean API design following REST principles
- **Database Integration**: PostgreSQL with JPA/Hibernate
- **Environment Configuration**: Secure configuration via environment variables
- **CORS Support**: Configured for frontend-backend communication
- **Docker Support**: Full containerization with Docker Compose
- **Health Checks**: Built-in health monitoring endpoints
- **Security**: Environment variables for sensitive data, proper .gitignore setup

### Infrastructure Features
- **Full Docker Stack**: Everything runs in containers
- **Automatic Database Setup**: Database schema initialized automatically
- **Service Health Checks**: Ensures services start in correct order
- **Volume Persistence**: Database data survives container restarts
- **Nginx Proxy**: Frontend proxies API calls to backend seamlessly
- **Production Ready**: Uses production-grade web server (Nginx)

## 🚦 Troubleshooting

### Common Issues

1. **"Cannot start backend - port already in use"**
   - Change ports in `backend/.env` file:
     ```env
     DB_PORT=5433
     APP_PORT=8081
     ```

2. **"Environment variables not found"**
   - Ensure `backend/.env` file exists
   - Check file has no spaces around `=` signs
   - Verify file encoding is UTF-8

3. **"Database connection failed"**
   - Check PostgreSQL container is running: `docker compose ps`
   - View database logs: `docker compose logs postgres`
   - Verify credentials in `backend/.env` file

4. **"Frontend cannot connect to API"**
   - Ensure backend is running and healthy: `curl http://localhost:8080/actuator/health`
   - Check `frontend/.env` has correct API URL
   - Verify CORS is properly configured in Spring Boot

5. **"npm start fails"**
   - Ensure Node.js 16+ is installed: `node --version`
   - Delete `node_modules` and run `npm install` again
   - Check for port conflicts (default is 3000)

6. **"Docker build fails"**
   - Ensure Docker Desktop is running
   - Try rebuilding without cache: `docker compose build --no-cache`
   - Check for sufficient disk space

### Reset Backend

If you encounter persistent backend issues:

```bash
# Stop and remove everything (⚠️ This deletes all data)
cd backend
docker compose down -v

# Start fresh
docker compose up --build
```

### Development Tips

- **Backend logs**: Use `docker compose logs -f app` to monitor Spring Boot
- **Database access**: Connect with `psql -h localhost -p 5432 -U <username> -d <database>`
- **API testing**: Use `curl http://localhost:8080/api/notes` or browser dev tools
- **Frontend debugging**: Use browser dev tools → Network tab for API calls

## 🏷️ Learn More

- [React Documentation](https://reactjs.org/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Create React App Documentation](https://facebook.github.io/create-react-app/docs/getting-started)

---

**NotaBene** - Hybrid development setup: Docker backend + local frontend! 🐳⚛️
