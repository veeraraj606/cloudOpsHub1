# auth-service

A production-style authentication & authorization microservice, built as the first service in the **CloudOpsHub** platform — a Jira/Trello-style project & task management system. This service handles user registration, login, JWT-based authentication, refresh-token rotation, role-based access control, and user administration.

CloudOpsHub also includes a `gateway-service` module (Spring Cloud Gateway, MVC variant) that routes external traffic to the backend services, and a `project-service` module for managing projects. Requests to `/api/auth/**` and `/api/users/**` go to `auth-service` (port `8081`); `/api/projects/**` goes to `project-service` (port `8082`). Both can be reached directly or through the gateway (port `8080`), which forwards them unchanged. `project-service` has no login of its own — it trusts JWTs issued by `auth-service` via a shared signing secret, verifying them independently with no shared database or user lookup.

## Tech Stack

- **Java 21**, **Spring Boot 4**
- **Spring Security** — JWT authentication, role-based method security (`@PreAuthorize`)
- **Spring Data JPA / Hibernate** — persistence
- **MySQL 8** + **Flyway** — versioned schema migrations
- **JJWT** — JWT generation/validation
- **Bean Validation** — request validation (`@NotBlank`, `@Email`, `@Size`)
- **Swagger / OpenAPI** — interactive API docs
- **Spring Boot Actuator** — health checks
- **JUnit 5 + Mockito + AssertJ** — unit testing
- **Docker / Docker Compose** — containerized local run

## Features

- User registration with BCrypt password hashing
- Login issuing a short-lived JWT access token + a rotating refresh token
- Refresh-token rotation: each use revokes the old token and issues a new one
- Logout (revokes the refresh token)
- Role-based authorization (`User` / `Admin`) via JWT claims
- User profile view/update, password change
- Admin endpoints: paginated user list, keyword search, enable/disable users
- Centralized exception handling with a consistent JSON error shape
- CORS configured for a future Angular frontend
- Config/secrets externalized via environment variables

## API Endpoints

### Auth (`/api/auth`) — public

| Method | Endpoint | Description |
|---|---|---|
| POST | `/register` | Create a new user |
| POST | `/login` | Authenticate, returns access + refresh token |
| POST | `/refresh` | Exchange a valid refresh token for a new token pair |
| POST | `/logout` | Revoke a refresh token |

### Users (`/api/users`) — requires authentication

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/me` | User | Current user's profile |
| PUT | `/me` | User | Update current user's profile |
| PUT | `/password` | User | Change password |
| GET | `/admin/dashboard` | Admin | Sample admin-only route |
| GET | `` | Admin | Paginated list of all users |
| GET | `/search?keyword=` | Admin | Search users by username/email |
| PUT | `/{id}/status` | Admin | Enable/disable a user |

### Ops

| Method | Endpoint | Description |
|---|---|---|
| GET | `/actuator/health` | Health check |
| GET | `/swagger-ui/index.html` | Interactive API docs |

## Project Service Endpoints

`project-service` (port `8082`) — all require a valid JWT issued by `auth-service`; results are scoped to the authenticated user.

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/projects` | Create a project (owned by the current user) |
| GET | `/api/projects` | List the current user's projects |
| GET | `/api/projects/{id}` | Get one of the current user's projects |

## Running Locally (without Docker)

Requires a local MySQL instance and Java 21.

```bash
# create the database
mysql -u root -p -e "CREATE DATABASE cloudopshub_auth"

# set environment variables (or rely on the localhost defaults in application.properties)
export DB_PASSWORD=yourpassword
export JWT_SECRET=your-long-random-secret

mvn spring-boot:run
```

The app starts on `http://localhost:8081`. Flyway applies migrations automatically on startup.

### Running the Gateway and Project Service alongside it

`gateway-service` and `project-service` are separate Maven modules in the same repo. With `auth-service` already running, start both (`mvn spring-boot:run` from each folder, or run their Application classes in your IDE). `project-service` needs its own database (`CREATE DATABASE cloudopshub_project`) and the *same* `JWT_SECRET` value as `auth-service` — that shared secret is what lets it trust tokens `auth-service` issues. `gateway-service` listens on `http://localhost:8080` and forwards `/api/auth/**` + `/api/users/**` to `auth-service` (`8081`) and `/api/projects/**` to `project-service` (`8082`).

## Running with Docker (recommended)

From the `cloudOpsHub` repo root (one level above this folder):

```bash
cp .env.example .env
# edit .env and set MYSQL_ROOT_PASSWORD and JWT_SECRET

docker compose up --build
```

This starts MySQL, `auth-service`, `project-service`, and `gateway-service` together — no local MySQL install needed. Once all containers are healthy: `auth-service` on `http://localhost:8081`, `project-service` on `http://localhost:8082`, and the gateway (routing to both) on `http://localhost:8080`.

## Running Tests

```bash
mvn test
```

Unit tests cover `AuthService` (registration, login, credential validation) and `RefreshTokenService` (creation, expiry, revocation, rotation) with the database mocked out — no live DB required to run them.

## Database Schema

Managed via Flyway migrations in `src/main/resources/db/migration/`:

- `V1` — `users` table
- `V2` — `refresh_tokens` table
- `V3` — adds `revoked` flag to `refresh_tokens`

## Project Status

Core `auth-service` functionality is complete: registration, login, JWT auth, refresh-token rotation, role-based authorization, user administration, validation, centralized error handling.

An API Gateway (`gateway-service`) routes `/api/auth/**`, `/api/users/**`, and `/api/projects/**` to the right backend service. A second business service, `project-service`, is live and independently verifies JWTs issued by `auth-service` — no shared database or user table between them, just a shared signing secret.

The full stack (MySQL + all three services) runs together via Docker Compose and has been verified working end-to-end, including on a fresh machine with no prior local setup.

Next planned: additional services behind the same gateway (e.g. a Task Service), plus CI/CD and cloud deployment.
