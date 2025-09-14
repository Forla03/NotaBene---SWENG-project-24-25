# MANUALE SVILUPPATORE - NOTA BENE

Questo manuale vuole essere una guida per guidare nell'avvio del progetto NotaBene.

## Stack utilizzato

Per lo sviluppo del progetto è stato scelto uno stack moderno e bilanciato, che combina stabilità lato backend e flessibilità lato frontend. L'idea è di mantenere il setup semplice ma realistico, in modo da poter riprodurre scenari tipici di un'applicazione enterprise (con API, database e interfaccia web reattiva) senza però complicare troppo l'ambiente di sviluppo.

- **Spring Boot 3** (Java 21)
- **PostgreSQL 16** (Docker)
- **React 18**
- **TypeScript** (local)
- **REST API**

## Prerequisiti

Per lo sviluppo del progetto è stato scelto un approccio ibrido: backend e database PostgreSQL sono contenuti in Docker, mentre il frontend viene eseguito direttamente in console con npm. Per avviare NotaBene, è dunque necessario avere installato sulla propria macchina almeno le seguenti tecnologie:

- **Docker Desktop** (con Docker Compose) ed essersi autenticati all'interno di questo
- **Node.js 16+** e npm – per il frontend
- **Git** – per clonare il repository

Nel caso in cui si debbano ancora installare:

- **Git download**: visitare [questa pagina](https://git-scm.com/downloads) e seguire le istruzioni
- **Node download**: visitare [questa pagina](https://nodejs.org/) e seguire le istruzioni
- **Docker Desktop download**: visitare [questa pagina](https://www.docker.com/products/docker-desktop/) e completare l'installazione, aprire Docker Desktop e registrarsi (operazione molto rapida, si può accedere direttamente anche con il proprio account GitHub o Google)

## Ottenere il codice

Per ottenere il codice bisogna clonare la repository da GitHub manualmente scaricandola dal sito oppure tramite i comandi qui sotto, eseguirli nel terminale all'interno della cartella dove si vuole salvare NotaBene.

```bash
git clone <repository-url>
cd NotaBene---SWENG-project-24-25
```

## Configurazioni locali

I file `.env` sono presenti sia nella cartella `backend` che `frontend`. Sono necessari per un corretto funzionamento del software. Di solito, questi vengono inclusi nel file `.gitignore` in quanto contengono informazioni sensibili. Tuttavia, è stato deciso di includerli nel versionamento del software poiché contengono soltanto dati fittizi utili solo ad avviare l'applicazione. In questo modo, l'avvio è anche più rapido dopo la clonazione della repository in quanto non vi sono files da creare manualmente.

## Avvio dell'applicazione

Di seguito si illustrano le istruzioni per avviare correttamente tutti i componenti del software.

### Avviare il backend

Dopo aver soddisfatto i prerequisiti, spostarsi nella cartella `backend` ed usare docker compose per buildare ed avviare il container.

```bash
cd backend
docker compose up --build
```

Attendi nei log la riga di avvio dell'app **"Started NotaBeneApplication"** che conferma il completo avvio del backend. Eventualmente, controllare anche dall'interfaccia per il desktop se è stato avviato correttamente.

### Avviare il frontend

In un altro terminale, spostarsi nella cartella `frontend` ed installare le dipendenze. Dopodiché, avviare l'applicazione; è sufficiente eseguire i comandi illustrati di seguito.

```bash
cd frontend
npm install
npm start
```

## Accesso rapido

Per l'accesso rapido all'applicazione utilizzare questi link, se la porta del backend è stata modificata nel file `.env` modificare il link con la porta giusta.

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **Health check**: http://localhost:8080/actuator/health

## Chiusura Applicazione

Per chiudere l'applicazione bisogna:

1. Nel terminale dove si è avviato il frontend fare `Ctrl + C`
2. Nel terminale dove si è avviato il backend fare `Ctrl + C` e poi eseguire il comando:

```bash
docker compose down
```

## Altri comandi utili per Docker

```bash
# Ricostruire completamente l'immagine
docker compose build --no-cache

# Vedere i container attivi
docker compose ps

# Vedere i log del backend
docker compose logs app

# Vedere i log del database
docker compose logs postgres

# Eliminare tutti i volumi (reset completo)
docker compose down -v
```

## Struttura del progetto

```
NotaBene---SWENG-project-24-25/
├── backend/                    # Backend Spring Boot
│   ├── src/main/java/         # Codice sorgente Java
│   ├── src/test/java/         # Test unitari e di integrazione
│   ├── docker-compose.yml     # Configurazione Docker
│   ├── Dockerfile             # Immagine Docker backend
│   ├── pom.xml                # Dipendenze Maven
│   └── .env                   # Variabili d'ambiente backend
├── frontend/                  # Frontend React TypeScript
│   ├── src/                   # Codice sorgente React
│   ├── public/                # Asset statici
│   ├── package.json           # Dipendenze npm
│   └── .env                   # Variabili d'ambiente frontend
└── README.md                  # Questo file
```

## Sicurezza e autenticazione

Il progetto utilizza Spring Security per proteggere tutte le API REST, garantendo che solo utenti autenticati possano accedere alle risorse sensibili.

### Autenticazione tramite token

Ogni richiesta all'API deve includere un token (header `X-Auth-Token`). I token vengono gestiti da `TokenStore`, che mantiene una mappa in memoria tra token e username. Questo permette di validare velocemente le richieste e di associare ciascuna al relativo utente.

### Filtro di sicurezza personalizzato

`TokenAuthenticationFilter` intercetta tutte le richieste e verifica il token. Le rotte pubbliche (login, registrazione, monitoraggio `/actuator/health`) sono escluse dal controllo. In caso di token valido, l'utente viene autenticato nel contesto di sicurezza Spring, altrimenti la richiesta viene bloccata con codice 401 Unauthorized.

### Configurazione centralizzata

`SecurityConfig` definisce le regole principali, tra cui:

- Disabilitazione del CSRF per le API REST
- Gestione delle richieste CORS, consentendo origini e metodi specifici (utile per l'integrazione con il frontend React)
- Esposizione dei token tramite header, in modo che il frontend possa leggerli
- Registrazione del filtro `TokenAuthenticationFilter` nel chain di Spring Security

### Password sicure

Le password vengono codificate con BCrypt, un algoritmo di hashing robusto, tramite il bean `PasswordEncoder`.

## Moduli Backend

### Modulo Controller

Il modulo controller espone le API REST dell'applicazione tramite classi annotate con `@RestController`. I controller orchestrano i servizi applicativi, validano gli input e convertono gli esiti in risposte HTTP coerenti, mantenendo la logica di business nei servizi.

Ogni controller è mappato sotto un prefisso stabile (`/api/notes`, `/api/folders`, `/api/tags`, `/api/auth`, `/api/health`). Le azioni seguono la semantica HTTP: GET per letture, POST per creazioni, PUT per aggiornamenti, DELETE per rimozioni. Le risposte sono restituite come `ResponseEntity` con codici di stato consistenti: 201 Created per creazioni, 204 No Content per eliminazioni, 400/403/404 per errori previsti.

#### AuthController (base path: `/api/auth`)

Autenticazione utente: registrazione e login; restituisce un token per le chiamate successive.

- `POST /register` — register()
- `POST /login` — login()

#### HealthController (base path: `/api`)

Verifica dello stato dell'applicazione (readiness/liveness).

- `GET /health` — health()

#### NoteController (base path: `/api/notes`)

CRUD delle note, ricerca semplice/avanzata, liste rapide e gestione permessi.

- `POST /` — createNote()
- `GET /` — getAllNotes()
- `GET /{id}` — getNoteById()
- `PUT /{id}` — updateNote()
- `DELETE /{id}` — deleteNote()
- `POST /{id}/copy` — copyNote()
- `GET /search` — searchNotes()
- `GET /search/advanced` — searchNotesAdvanced()
- `GET /created` — getCreatedNotes()
- `GET /shared` — getSharedNotes()

#### NoteVersionController (base path: `/api/notes/{noteId}/versions`)

Storico versioni, dettaglio, confronto e ripristino di una nota.

- `GET /` — getVersionHistory()
- `GET /{versionNumber}` — getVersion()
- `POST /{versionNumber}/restore` — compareVersionsEnhanced()

#### FolderController (base path: `/api/folders`)

Gestione cartelle, associazione/disassociazione note e ricerche contestuali.

- `GET /` — listMyFolders()
- `POST /` — create()
- `GET /{id}` — get()
- `DELETE /{id}` — delete()
- `POST /{folderId}/notes/{noteId}` — addNote()
- `DELETE /{folderId}/notes/{noteId}` — removeNote()
- `GET /{folderId}/notes/search` — searchNotesInFolder()

#### TagController (base path: `/api/tags`)

Gestione essenziale dei tag (elenco e creazione).

- `GET /` — list()
- `POST /` — create()

### Modulo DTO

La cartella `dto` definisce il contratto stabile tra backend Spring e frontend React: oggetti semplici e serializzabili (JSON) usati solo ai bordi dell'applicazione (ingresso/uscita dei controller). I DTO proteggono le entità JPA, applicano validazione precoce sugli input e forniscono proiezioni mirate per la UI, evitando leakage di dettagli interni (hash password, lazy fields, ecc.).

### Modulo Entity

La cartella `entity` contiene il modello di dominio dell'applicazione: le classi annotate JPA che mappano gli oggetti di business alle tabelle PostgreSQL. Qui vivono gli oggetti "veri" che il resto del sistema manipola (es. utenti, note, versioni delle note) ed è da queste entità che i repository Spring Data JPA leggono e scrivono sul database.

### Modulo Exception

La cartella `exception` centralizza la gestione degli errori API con `@RestControllerAdvice` e mappa le eccezioni a status HTTP coerenti restituendo un payload uniforme.

Mappature principali:

- `NoteNotFoundException` → 404 Not Found
- `UnauthorizedNoteAccessException` → 403 Forbidden
- `DataIntegrityViolationException` → 409 Conflict
- `MethodArgumentNotValidException` → 400 Bad Request
- `Exception` generica → 500 Internal Server Error

### Modulo Service

Nel modulo `service` vive il "cervello" dell'applicazione: qui si decidono i casi d'uso, si applicano le regole di permesso e si orchestrano repository e DTO. I controller restano sottili, perché tutto ciò che riguarda cosa è consentito, come validare i dati e come comporre la risposta per la UI accade a questo livello.

#### Sottosezioni del modulo Service:

- **Support**: Componenti iniettabili che fanno da ponte tra contesto di sicurezza e persistenza
- **Memento**: Implementazione del pattern GoF Memento per catturare e ripristinare lo stato completo di una nota

## Frontend

Il frontend è una SPA React 18 + TypeScript organizzata per feature component con servizi HTTP centralizzati. La regola è semplice: i componenti presentano la UI e orchestrano piccoli stati locali; tutte le chiamate passano da `services/api`, che espone tipizzazioni (`Note`, `TagDTO`, ecc.) e funzioni per note, cartelle, versioni e autenticazione.

### Caratteristiche principali:

- **Autenticazione**: Form controllati con validazione e gestione errori HTTP
- **Gestione Note**: CRUD completo con permessi, condivisione e versioning
- **Ricerca**: Semplice e avanzata con filtri combinati
- **Cartelle e Tag**: Organizzazione e classificazione delle note
- **Interfaccia**: CSS per-componente senza framework UI pesanti

## Pattern e principi adottati

Il progetto implementa diversi pattern architetturali per garantire un design pulito e manutenibile:

### Pattern GRASP

- **Information Expert**: Responsabilità assegnate a chi possiede le informazioni
- **Creator**: Creazione oggetti dove ha più senso
- **Controller**: Gestione delle operazioni di sistema
- **Low Coupling**: Basso accoppiamento tramite interfacce
- **High Cohesion**: Alta coesione con responsabilità focalizzate

### Pattern GoF

- **Strategy**: Per algoritmi intercambiabili
- **Template Method**: Nei filtri di sicurezza
- **Memento**: Per lo stato delle note
- **Repository/DAO**: Per l'astrazione della persistenza
- **Facade**: Nel frontend per le API
- **Factory Method**: Nei DTO

## Test

### Backend (JUnit + Spring Test)

I test sono presenti in: `backend/src/test/java/...`

**Esecuzione locale:**

```bash
cd backend
mvn test
```

Nel progetto sono presenti sia unit test mirati sia integration test end-to-end:

- **Unit test**: Isolano singoli componenti (es. `NoteControllerTest` con `@WebMvcTest`)
- **Integration test**: Testano l'intero stack (es. `NoteIntegrationTest` con `@SpringBootTest`)

#### Note importanti per i test dei Controller

Gli endpoint (tranne `/api/auth/**` e `/actuator/health`) richiedono token.

Nei test con MockMvc, imposta l'header `X-Auth-Token` con un token noto allo store oppure configura il contesto di sicurezza per bypassare il filtro.

Se nei test vedi 403/401, controlla:

- Presenza header `X-Auth-Token` coerente con lo store usato nel test
- Import/registrazione del filtro `TokenAuthenticationFilter` quando serve
- Se usi `@WebMvcTest`, valuta `addFilters = false` o config mirata

### Frontend

```bash
cd frontend
npm test
```

## Troubleshooting

### Backend non parte (porta occupata)

Modifica porte in `backend/.env`, es.: `APP_PORT=8081`, `DB_PORT=5433` e riavvia.

### Variabili d'ambiente non lette

Controlla che `backend/.env` esista, senza spazi attorno a `=` e con encoding UTF-8.

### DB non raggiungibile

```bash
docker compose ps          # verifica container
docker compose logs postgres  # controlla credenziali
```

### Frontend non raggiunge l'API

```bash
curl http://localhost:8080/actuator/health  # backend UP
```

Controlla `frontend/.env` (`REACT_APP_API_URL`), CORS e il token.

### Chiamate API → 403/401

Aggiungi `X-Auth-Token` (il frontend lo fa via interceptor se il token è salvato). Nei test MockMvc ricordati di impostare l'header.

### Build Docker fallisce

- Assicurati che Docker Desktop sia attivo
- Prova `docker compose build --no-cache`
- Verifica spazio disco

### Reset completo backend

```bash
cd backend
docker compose down -v
docker compose up --build
```