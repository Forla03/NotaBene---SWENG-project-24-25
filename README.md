# NotaBene — SWENG Project 2024/25

**NotaBene** is a collaborative web application that allows users to share short text notes (maximum 280 characters) with other users. This full-stack project consists of a Spring Boot backend and a React TypeScript frontend.

## 🏗️ Project Architecture

- **Backend**: Spring Boot 3.x with Java 21
- **Frontend**: React 18+ with TypeScript
- **Database**: PostgreSQL (via Docker)
- **API**: RESTful services
- **Build Tools**: Maven (backend), npm (frontend)

## 📋 Requirements

- **Java 21**
- **Node.js 16+** & npm
- **Docker & Docker Compose**
- **Maven**
- **PostgreSQL** (via Docker)

## 🚀 Quick Start

### 1. Clone and Setup

```bash
git clone <repository-url>
cd NotaBene---SWENG-project-24-25
```

### 2. Backend Setup

#### Database Configuration

1. **Start PostgreSQL with Docker**
   ```bash
   cd backend
   docker compose up -d
   ```

2. **Create environment file**
   
   Create a `.env` file in the `backend/` folder:

   ```env
   POSTGRES_DB=your_database_name
   POSTGRES_USER=your_username
   POSTGRES_PASSWORD=your_password
   ```

3. **Configure your IDE**
   
   For IntelliJ IDEA:
   - Install "EnvFile" plugin from marketplace
   - Create a run configuration and add your `.env` file

4. **Run the Spring Boot application**
   ```bash
   ./mvnw spring-boot:run
   ```
   
   The backend will be available at `http://localhost:8080`

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
│   ├── docker-compose.yml
│   ├── pom.xml
│   ├── .env (create this)
│   └── src/
│       ├── main/java/com/example/myspringapp/
│       └── resources/application.properties
└── frontend/
    ├── package.json
    ├── .env
    ├── public/
    └── src/
        ├── components/NotesList.tsx
        ├── services/api.ts
        ├── App.tsx
        └── App.css
```

## 🛠️ Available Scripts

### Backend (Maven)

```bash
cd backend
./mvnw spring-boot:run    # Run the application
./mvnw test               # Run tests
./mvnw clean package      # Build the project
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
- **Environment Configuration**: Secure configuration via environment variables
- **CORS Support**: Configured for frontend-backend communication

## 🔧 Development

### Docker Compose Configuration

The `docker-compose.yml` file includes:

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: your_database_name
      POSTGRES_USER: your_username
      POSTGRES_PASSWORD: your_password
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

volumes:
  pgdata:
```

### Environment Variables

**Backend (.env):**
```env
POSTGRES_DB=your_database_name
POSTGRES_USER=your_username
POSTGRES_PASSWORD=your_password
```

**Frontend (.env):**
```env
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_NAME=NotaBene Frontend
```

## 🚦 Running the Complete Application

1. **Start the database**
   ```bash
   cd backend
   docker compose up -d
   ```

2. **Start the backend** (in your IDE or terminal)
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

3. **Start the frontend**
   ```bash
   cd frontend
   npm start
   ```

4. **Access the application**
   - **Frontend**: `http://localhost:3000`
   - **Backend API**: `http://localhost:8080/api`

## 📝 Notes

- The frontend automatically proxies API requests to the backend
- Make sure both backend and frontend are running for full functionality
- The application uses CORS configuration to allow frontend-backend communication
- Database data persists in Docker volumes

## 🏷️ Learn More

- [React Documentation](https://reactjs.org/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Create React App Documentation](https://facebook.github.io/create-react-app/docs/getting-started)
- [TypeScript Documentation](https://www.typescriptlang.org/)

---
