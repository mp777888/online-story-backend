# Online Stories Backend

Backend for an online reading and story-writing platform built with Spring Boot microservices. The system includes user management, authentication, story/chapter publishing, media upload, payments, reports, recommendations, and AI-assisted writing/search features.

## Architecture

The project is organized as a monorepo. Each service is an independent Spring Boot application with its own `pom.xml`, `Dockerfile`, and runtime configuration.

| Module | Port | Purpose |
| --- | ---: | --- |
| `api-gateway` | 9000 | Single entry point, route requests to backend services |
| `service-registry` | 8761 | Eureka service registry |
| `authentication-service` | 8082 | Login, social login, refresh token, logout, forgot password |
| `user-service` | 8081 | User profile, follow, notification, check-in, recommendation preferences |
| `story-service` | 8083 | Stories, chapters, drafts, versions, genres, tags, interactions, reading history |
| `media_service` | 8084 | Cloudinary upload/delete APIs |
| `transaction-service` | 8085 | Wallet, VNPay/MoMo payments, unlock story, payout history |
| `recommend-service` | 8086 | Search, genre recommendation, similar story recommendation |
| `report-service` | 8087 | User reports and admin report handling |
| `ai-service` | 8088 | AI chat, spelling correction, content suggestion, image generation, plagiarism check |
| `common-lib` | - | Shared DTOs, events, exceptions, Kafka topics, utilities |

Supporting infrastructure:

| Component | Port | Purpose |
| --- | ---: | --- |
| Keycloak | 8181 | Identity and access management |
| Kafka | 9092 / 29092 | Event bus |
| Kafka UI | 8089 | Kafka monitoring UI |
| Zookeeper | 2181 | Kafka dependency |

## Main Technologies

- Java 21, Spring Boot, Spring Cloud, Spring Security
- Eureka, API Gateway, OpenFeign
- MongoDB, Redis, OpenSearch
- Kafka
- Keycloak
- Cloudinary
- VNPay, MoMo
- Gemini, Cohere, Azure Speech
- Docker and Docker Compose

## Repository Structure

```text
.
|-- ai-service
|-- api-gateway
|-- authentication-service
|-- common-lib
|-- docker
|-- media_service
|-- recommend-service
|-- report-service
|-- service-registry
|-- social-service
|-- story-service
|-- transaction-service
|-- user-service
|-- docker-compose.yml
`-- docker-compose-deploy.yml
```

## Prerequisites

Required for Docker-based local setup:

- Docker Desktop
- Docker Compose

Required only when running services directly outside Docker:

- Java 21
- Maven 3.x

Check versions:

```bash
docker --version
docker compose version
java -version
mvn -version
```

## Environment Variables

Create a `.env` file in the project root before running Docker Compose. Do not commit real secrets.

Required groups:

```env
# Keycloak database
DATABASE_HOST=
DATABASE_NAME=
DATABASE_USERNAME=
DATABASE_PASSWORD=

# MongoDB
MONGODB_URI=
USER_MONGODB_DB=
STORY_MONGODB_DB=
TRANSACTION_MONGODB_DB=
RECOMMEND_MONGODB_DB=
REPORT_MONGODB_DB=
AI_MONGODB_DB=

# Keycloak client
REALM=online-stories-realm
CLIENT_ID=
CLIENT_SECRET=
KEYCLOAK_REDIRECT_URI=

# Redis / OpenSearch
REDIS_URL=
OPENSEARCH_URL=

# AI providers
GEMINI_API_KEY=
GEMINI_PROJECT_ID=
COHERE_API_KEY=

# Media
CLOUDINARY_CLOUD_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=

# Text to speech
AZURE_SPEECH_KEY=
AZURE_SPEECH_REGION=

# Payments
VN_PAY_TMN_CODE=
VN_PAY_SECRET_KEY=
VN_PAY_RETURN_URL=
MOMO_PARTNER_CODE=
MOMO_ACCESS_KEY=
MOMO_SECRET_KEY=
MOMO_REDIRECT_URL=
MOMO_IPN_URL=

# Deploy compose only
IP_ADDRESS=
```

## Running Locally

### 1. Start infrastructure only

Use this when developing services directly from the IDE or terminal. It starts Keycloak, Kafka, Zookeeper, and Kafka UI.

```bash
docker compose up -d
```

Useful URLs:

- Keycloak: http://localhost:8181
- Kafka UI: http://localhost:8089

Stop infrastructure:

```bash
docker compose down
```

### 2. Start the full backend stack

Use the deploy compose file to build and run all backend services.

```bash
docker compose -f docker-compose-deploy.yml up -d --build
```

Check containers:

```bash
docker compose -f docker-compose-deploy.yml ps
```

View logs:

```bash
docker compose -f docker-compose-deploy.yml logs -f
docker compose -f docker-compose-deploy.yml logs -f api-gateway
docker compose -f docker-compose-deploy.yml logs -f story-service
```

Stop the full stack:

```bash
docker compose -f docker-compose-deploy.yml down
```

## Running a Service Manually

Start infrastructure first:

```bash
docker compose up -d
```

Install the shared library:

```bash
cd common-lib
mvn clean install -DskipTests
```

Run a service:

```bash
cd ../story-service
mvn spring-boot:run
```

Repeat with any service directory as needed.

## API Gateway Routes

All public backend APIs are routed through `api-gateway` at `http://localhost:9000`.

| Prefix | Service |
| --- | --- |
| `/api/auth/**` | `authentication-service` |
| `/api/users/**` | `user-service` |
| `/api/stories/**` | `story-service` |
| `/api/media/**` | `media-service` |
| `/api/transactions/**` | `transaction-service` |
| `/api/recommend/**` | `recommend-service` |
| `/api/reports/**` | `report-service` |
| `/api/ai/**` | `ai-service` |

## Event Topics

Shared Kafka topics are defined in `common-lib`.

| Topic | Purpose |
| --- | --- |
| `story.updated.v1` | Sync story data to recommendation/search |
| `story.metrics-updated.v1` | Sync story metrics |
| `chapter.published.v1` | Chapter published event |
| `chapter.published.v2` | Chapter approved/published event |
| `chapter.sync.v1` | Sync chapter content to AI/vector store |
| `report.responded.v1` | Report response notification |
| `user.created.v1` | Sync new user data |
| `user.updated.v1` | Sync updated user data |
| `transaction.event.v1` | Transaction event |

## Development Notes

- Keep real secrets out of Git. Use local `.env` files or secret managers.
- `docker-compose.yml` is intended for infrastructure-only local development.
- `docker-compose-deploy.yml` is intended for building and running the full backend stack.
- If a service fails to start, check Eureka registration, Keycloak issuer URL, Kafka bootstrap server, and required environment variables.
- If running a service outside Docker, make sure its `application.yml` or environment variables point to `localhost` infrastructure services.

## Troubleshooting

Rebuild one or more services:

```bash
docker compose -f docker-compose-deploy.yml build story-service api-gateway
docker compose -f docker-compose-deploy.yml up -d story-service api-gateway
```

Remove stopped containers and networks:

```bash
docker compose -f docker-compose-deploy.yml down
```

Check whether services are registered:

- Eureka dashboard: http://localhost:8761

Check Kafka messages/topics:

- Kafka UI: http://localhost:8089
