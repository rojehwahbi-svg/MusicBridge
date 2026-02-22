# MusicBridge - TIDAL Sync & Search API

Spring Boot application that synchronizes artists and albums from TIDAL API to a PostgreSQL database.

## 📋 Table of Contents
- [Requirements](#requirements)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Architecture](#architecture)

## 🔧 Requirements

- **Java**: JDK 17 or higher
- **Docker**: Docker Desktop 20.10+ (for PostgreSQL)
- **Maven**: 3.8+ (or use included Maven Wrapper)
- **TIDAL API Credentials**: Client ID and Client Secret

## 🚀 Quick Start

### 1. Clone Repository
```bash
git clone <repository-url>
cd Musicbridge
```

### 2. Setup Environment Variables
```bash
# Copy example environment file
cp .env.example .env

# Edit .env and add your TIDAL API credentials
# REQUIRED:
#   TIDAL_CLIENT_ID=your_client_id
#   TIDAL_CLIENT_SECRET=your_client_secret
```

### 3. Start Database
# start dockerdesktop and run the following command to start PostgreSQL using Docker Compose:
```bash
# Start PostgreSQL with Docker Compose
docker-compose up -d

# Verify database is running
docker ps
```

### 5. Run Application
```bash
# Using Maven Wrapper (recommended)
./mvnw spring-boot:run

# Or with installed Maven
mvn spring-boot:run
```

The application starts on **http://localhost:8080**

---

## 🎯 Quick Start Guide - Using the API

### Step 1: Sync Music Data from TIDAL
```bash
# Sync "best rock songs" (default: 50 tracks)
curl -X POST http://localhost:8080/api/sync/trigger

# Or custom query and track limit
curl -X POST "http://localhost:8080/api/sync/trigger?query=jazz&trackLimit=100"
```

This will:
1. Search TIDAL for 50-100 tracks matching your query
2. Extract all unique artists from results
3. Fetch each artist's albums
4. Save everything (Artists and Albums) to the database

### Step 2: Search for Artists
```bash
# Find "The Beatles"
curl "http://localhost:8080/api/search/artists?q=beatles"

# Find "Metallica"
curl "http://localhost:8080/api/search/artists?q=metallica&page=0&size=20"
```

### Step 3: Get Artist Details & Albums
```bash
# Get artist UUID from search, then fetch full details
curl "http://localhost:8080/api/artists/9d1a24f7-8b3d-4bff-aedf-aaa3586b0b70"

# Returns artist with all albums
```

### Step 4: Search for Albums
```bash
# Find albums containing "master"
curl "http://localhost:8080/api/search/albums?q=master&page=0&size=20"

# Search all (artists + albums)
curl "http://localhost:8080/api/search?q=pink+floyd&page=0&size=10"
```

---

### Environment Variables

All configuration is done via environment variables. See [ENV_VARIABLES.md](ENV_VARIABLES.md) for complete documentation.

**Required Variables:**
| Variable | Description |
|----------|-------------|
| `TIDAL_CLIENT_ID` | Your TIDAL API Client ID |
| `TIDAL_CLIENT_SECRET` | Your TIDAL API Client Secret |

**Optional Variables (with defaults):**
| Variable | Default | Description |
|----------|---------|-------------|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/your_db` | Database JDBC URL |
| `DATABASE_USER` | `your_user` | Database username |
| `DATABASE_PASSWORD` | `your_password` | Database password |
| `TIDAL_SEARCH_QUERY` | `best rock songs` | Default search query for `/api/sync/trigger` |
| `TIDAL_TRACK_LIMIT` | `50` | Default track limit (1-500) |
| `TIDAL_SYNC_ON_STARTUP` | `false` | Enable automatic sync when app starts |
| `TIDAL_SYNC_SCHEDULED_ENABLED` | `false` | Enable scheduled/recurring sync |
| `TIDAL_SYNC_CRON` | `0 0 2 * * *` | Cron schedule (every day at 2 AM) |

### Database Configuration

PostgreSQL runs in Docker and is configured via `docker-compose.yml`:
```yaml
services:
  postgres:
    image: postgres:16
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-your_db}
      POSTGRES_USER: ${POSTGRES_USER:-your_user}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-your_password}
```

Database migrations are managed by **Flyway** and run automatically on startup.

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api
```

### 🔍 Search API

The search API uses the **`q` parameter** (not `query`).

#### Search All (Artists + Albums)
```http
GET /api/search?q=beatles&page=0&size=10
```

**Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `q` | string | ✅ Yes | — | Search query (artist name or album title) |
| `page` | integer | ❌ No | 0 | Page number (0-indexed) |
| `size` | integer | ❌ No | 20 | Results per page (1-100) |

**Examples:**
```bash
# Search for "The Beatles" and related albums
curl "http://localhost:8080/api/search?q=beatles&page=0&size=10"

# Search for "rock" music
curl "http://localhost:8080/api/search?q=rock&page=0&size=20"

# Get second page (20-40 results)
curl "http://localhost:8080/api/search?q=metal&page=1&size=20"
```

**Response Example:**
```json
{
  "artists": {
    "content": [
      {
        "id": "9d1a24f7-8b3d-4bff-aedf-aaa3586b0b70",
        "name": "The Beatles",
        "tidal_id": "3634161",
        "albums": [],
        "created_at": "2026-02-21T18:02:32.161969",
        "updated_at": "2026-02-21T18:02:32.161969"
      }
    ],
    "page": {
      "size": 10,
      "number": 0,
      "totalElements": 1,
      "totalPages": 1
    }
  },
  "albums": {
    "content": [
      {
        "id": "ca87154f-6962-4ed2-982e-5d443f8a8911",
        "title": "Beatles '64 (Music from the Disney+ Documentary)",
        "tidal_id": "400380622",
        "artist_name": "The Beatles",
        "artist_id": "9d1a24f7-8b3d-4bff-aedf-aaa3586b0b70",
        "release_date": "2024-11-22",
        "created_at": "2026-02-21T18:02:32.167852",
        "updated_at": "2026-02-21T18:02:32.167852"
      }
    ],
    "page": {
      "size": 10,
      "number": 0,
      "totalElements": 3,
      "totalPages": 1
    }
  }
}
```

#### Search Artists Only
```http
GET /api/search/artists?q=metallica&page=0&size=20
```

**Examples:**
```bash
# Find all Metallica albums and details
curl "http://localhost:8080/api/search/artists?q=metallica&page=0&size=20"

# Search for "Pink Floyd"
curl "http://localhost:8080/api/search/artists?q=pink+floyd&page=0&size=10"
```

#### Search Albums Only
```http
GET /api/search/albums?q=puppets&page=0&size=20
```

**Examples:**
```bash
# Find albums with "Master" in title
curl "http://localhost:8080/api/search/albums?q=master&page=0&size=20"

# Search for specific album
curl "http://localhost:8080/api/search/albums?q=dark+side&page=0&size=10"
```

---

### 🎵 Artists API

#### Get All Artists (Paginated)
```http
GET /api/artists?page=0&size=20&sort=name
```

**Response:**
```json
{
  "content": [
    {
      "id": "uuid",
      "tidal_id": "12345",
      "name": "Metallica",
      "albums": [
        {
          "id": "album-uuid",
          "title": "Master of Puppets",
          "tidal_id": "album123",
          "release_date": "1986-03-03"
        }
      ],
      "created_at": "2024-01-01T00:00:00",
      "updated_at": "2024-01-01T00:00:00"
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

#### Get Artist by ID
```http
GET /api/artists/{id}
```

**Example:**
```bash
curl "http://localhost:8080/api/artists/9d1a24f7-8b3d-4bff-aedf-aaa3586b0b70"
```

#### Create Artist
```http
POST /api/artists
Content-Type: application/json

{
  "tidal_id": "12345",
  "name": "Artist Name"
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/artists \
  -H "Content-Type: application/json" \
  -d '{"tidal_id":"54321","name":"New Artist"}'
```

#### Update Artist
```http
PUT /api/artists/{id}
Content-Type: application/json

{
  "name": "Updated Name"
}
```

#### Delete Artist
```http
DELETE /api/artists/{id}
```

---

### 💿 Albums API

#### Get All Albums (Paginated)
```http
GET /api/albums?page=0&size=20&sort=title
```

**Response:**
```json
{
  "content": [
    {
      "id": "uuid",
      "tidal_id": "album123",
      "title": "Master of Puppets",
      "artist_name": "Metallica",
      "artist_id": "artist-uuid",
      "release_date": "1986-03-03",
      "created_at": "2024-01-01T00:00:00",
      "updated_at": "2024-01-01T00:00:00"
    }
  ]
}
```

#### Get Album by ID
```http
GET /api/albums/{id}
```

#### Create Album
```http
POST /api/albums
Content-Type: application/json

{
  "tidal_id": "album123",
  "title": "Album Title",
  "release_date": "2024-01-01",
  "artist_id": "artist-uuid"
}
```

#### Update Album
```http
PUT /api/albums/{id}
Content-Type: application/json

{
  "title": "Updated Title",
  "release_date": "2024-01-01"
}
```

#### Delete Album
```http
DELETE /api/albums/{id}
```

---

### 🔄 Sync API - TIDAL Integration

#### Trigger Manual Sync
```http
POST /api/sync/trigger
```

**Optional Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `query` | string | "best rock songs" | Search query on TIDAL |
| `trackLimit` | integer | 50 | Max tracks to fetch (1-500) |

**How it works:**
1. Searches TIDAL for tracks matching your query
2. Extracts all unique artists from the search results
3. For each artist, fetches their albums from TIDAL
4. Saves/updates all artists and albums in the database
5. Respects `manually_modified` flag (won't overwrite user edits)

**Examples:**

```bash
# Sync default: "best rock songs" with 50 tracks
curl -X POST http://localhost:8080/api/sync/trigger

# Sync metal music (100 tracks)
curl -X POST "http://localhost:8080/api/sync/trigger?query=metal&trackLimit=100"

# Sync jazz from Germany
curl -X POST "http://localhost:8080/api/sync/trigger?query=jazz&trackLimit=75"

# Sync electronic dance music
curl -X POST "http://localhost:8080/api/sync/trigger?query=edm&trackLimit=100"

# Sync top hits
curl -X POST "http://localhost:8080/api/sync/trigger?query=top+hits&trackLimit=200"
```

**Response:**
```json
{
  "status": "success",
  "message": "Sync completed successfully"
}
```

**Error Response (trackLimit too high):**
```json
{
  "error": "Track limit must be between 1 and 500"
}
```

---

### 🔍 How Artist Discovery Works

When you trigger `/api/sync/trigger`, MusicBridge performs this workflow:

```
User Request: /api/sync/trigger?query=rock&trackLimit=100
    ↓
1. TIDAL Search: Search for 100 tracks matching "rock"
    ↓
   Returns: Array of track objects with artist relationships
    ↓
2. Extract Artists: Get unique artist IDs from all 100 tracks
    ↓
   Example: If 100 rock tracks contain 25 unique artists
    ↓
3. Fetch Albums: For each of 25 artists, fetch all albums from TIDAL
    ↓
   Example: Metallica might have 15 albums, Iron Maiden 18, etc.
    ↓
4. Save to Database:
   ├── Insert/update 25 artists
   ├── Insert/update all albums for those artists
   └── Respect manually_modified flag (don't overwrite user edits)
    ↓
Response: "Sync completed successfully"
```

**Example Sync Scenario:**

```bash
# Sync "best rock songs" - discovers 500+ albums from ~50 artists
curl -X POST "http://localhost:8080/api/sync/trigger?query=best+rock+songs&trackLimit=100"

# After sync, search for artists
curl "http://localhost:8080/api/search/artists?q=led&page=0&size=10"
# Returns: Led Zeppelin, Led Zeppelin remixes, etc.

# Get Led Zeppelin's full discography
curl "http://localhost:8080/api/artists/led-zeppelin-uuid"
# Shows all albums synced from TIDAL
```

---

### 📋 Search Examples

```bash
# 1. Find all Beatles albums
curl "http://localhost:8080/api/search?q=beatles"

# 2. Find all rock albums (broad search)
curl "http://localhost:8080/api/search?q=rock"

# 3. Find specific album title
curl "http://localhost:8080/api/search/albums?q=dark+side+of+the+moon"

# 4. Find artist by name
curl "http://localhost:8080/api/search/artists?q=pink+floyd"

# 5. Pagination - get page 2 of results
curl "http://localhost:8080/api/search?q=metal&page=1&size=20"

# 6. Large result set (100 per page)
curl "http://localhost:8080/api/search?q=rock&page=0&size=100"
```

---

## 🧪 Testing

### Run All Tests
```bash
mvn test
```
### Test ist fot intern Only
### Test Coverage
- **48 tests** covering:
  - ✅ Service layer (ArtistService, AlbumService, SearchService)
  - ✅ Controller layer (ArtistController with @WebMvcTest)
  - ✅ Sync service (TidalSyncService)
  - ✅ Integration tests (Application context)

### Run Specific Test
```bash
mvn test -Dtest=ArtistServiceImplTest
```

## 🏗️ Architecture

### Technology Stack
- **Framework**: Spring Boot 3.5.10
- **Java Version**: 17
- **Database**: PostgreSQL 16
- **ORM**: Hibernate/JPA
- **Migrations**: Flyway
- **HTTP Client**: Spring WebClient
- **Testing**: JUnit 5, Mockito, MockMvc
- **Build Tool**: Maven

### Project Structure
```
src/
├── main/
│   ├── java/com/rowa/musicbridge/
│   │   ├── apis/              # REST API layer
│   │   │   ├── controller/    # REST controllers
│   │   │   ├── dto/           # Data Transfer Objects
│   │   │   ├── mapper/        # Entity <-> DTO mappers
│   │   │   └── service/       # Business logic
│   │   ├── domain/            # Domain layer
│   │   │   ├── entity/        # JPA entities
│   │   │   ├── repository/    # Spring Data repositories
│   │   │   └── exception/     # Custom exceptions
│   │   ├── integration/       # External APIs
│   │   │   └── tidal/         # TIDAL API client
│   │   ├── sync/              # Synchronization logic
│   │   └── config/            # Configuration classes
│   └── resources/
│       ├── application.yml    # Application config
│       └── db/migration/      # Flyway SQL migrations
└── test/                      # Unit & integration tests
```

### Database Schema

**Artists Table:**
```sql
CREATE TABLE artists (
    id UUID PRIMARY KEY,
    tidal_id VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    manually_modified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

**Albums Table:**
```sql
CREATE TABLE albums (
    id UUID PRIMARY KEY,
    tidal_id VARCHAR(255) UNIQUE NOT NULL,
    title VARCHAR(500) NOT NULL,
    artist_id UUID NOT NULL REFERENCES artists(id),
    artist_name VARCHAR(255) NOT NULL,  -- Denormalized
    release_date DATE,
    manually_modified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Key Features

1. **TIDAL API Integration**
   - OAuth 2.0 authentication with automatic token refresh
   - Search tracks by query (returns artist relationships)
   - Extract artists from track results
   - Fetch full discography for each artist
   - Country-specific search (Germany)
   - Configurable track limit per sync (1-500)

2. **Data Synchronization**
   - **Manual trigger via `/api/sync/trigger`** with custom query and track limit
   - Optional automatic sync on startup (`TIDAL_SYNC_ON_STARTUP`)
   - Optional scheduled sync (configurable cron via `TIDAL_SYNC_CRON`)
   - Respects `manually_modified` flag (won't overwrite user-edited data)
   - De-duplicates artists across search results
   - Intelligent incremental updates

3. **Search Functionality**
   - Full-text search on artists and albums
   - Fallback to LIKE-based search (case-insensitive)
   - Combined search (artists + albums in single request)
   - Search artists only or albums only
   - Pagination support (configurable page size)
   - **Important:** Uses `q` parameter (not `query`)

4. **Data Denormalization**
   - `artist_name` stored in albums table
   - Avoids expensive JOINs for better performance
   - Automatically updated during sync
   - Single query returns all needed album data

## 🔒 Security

- ✅ Secrets managed via environment variables
- ✅ `.env` file excluded from Git
- ✅ No hardcoded credentials in code
- ✅ Database passwords configurable per environment

## 📝 Additional Documentation

- [ENV_VARIABLES.md](ENV_VARIABLES.md) - Complete environment variable reference
- [CODE_REVIEW.txt](CODE_REVIEW.txt) - Code review findings and fixes

## 🐛 Troubleshooting

### Database Connection Failed
```bash
# Check if PostgreSQL is running
docker ps

# Restart database
docker-compose down
docker-compose up -d
```

### App Won't Start (Java Version Error)
```bash
# Verify Java version
java -version  # Should be 17+

# Use Maven Wrapper (uses correct Java)
./mvnw spring-boot:run
```

### TIDAL API Authentication Failed
```bash
# Verify credentials in .env file
cat .env | grep TIDAL_CLIENT

# Reload environment variables
.\load-env.ps1  # Windows
source .env     # Linux/Mac
```

### Port 8080 Already in Use
```bash
# Find process using port 8080
netstat -ano | findstr :8080  # Windows
lsof -i :8080                 # Linux/Mac

# Stop the process or change port in application.yml
```

## 📄 License

This project is for educational/evaluation purposes.

##  Author
Rojeh Music Solutions GmbH :D ^_^

