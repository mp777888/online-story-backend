# NovelToolKit Backend

Backend monorepo for an online story reading and writing platform. The system supports user management, authentication, story/chapter publishing, drafts and versions, media upload, payments, reports, recommendations, notifications, and AI-assisted writing features.

## Current Architecture

The backend is organized as a Spring Boot microservices system. Each service is an independent Maven project with its own `pom.xml`, Dockerfile, and runtime configuration. Shared DTOs, events, enums, exceptions, and Kafka topic names are kept in `common-lib`.

There are two Docker Compose files:

- `docker-compose.yml`: local infrastructure only, useful when running services from the IDE.
- `docker-compose-deploy.yml`: full backend stack for deployment, including Nginx reverse proxy.

### Active Services

| Component | Port | Exposure | Purpose |
| --- | ---: | --- | --- |
| `nginx-proxy` | 80 | Deploy public entry | Routes `/api/**` to `api-gateway` and `/` to Keycloak |
| `api-gateway` | 9000 | Internal in deploy, direct in local dev | Single backend entry point and route dispatcher |
| `service-registry` | 8761 | Direct in deploy | Eureka service discovery |
| `authentication-service` | 8082 | Through gateway | Login, social login, token refresh, logout, forgot password |
| `user-service` | 8081 | Through gateway | User profile, follow, notification, check-in, user preference data |
| `story-service` | 8083 | Through gateway | Stories, chapters, drafts, versions, genres, tags, ratings, comments, reading history |
| `media_service` | 8084 | Through gateway | Cloudinary upload/delete APIs |
| `transaction-service` | 8085 | Through gateway | Wallet, VNPay/MoMo payments, story unlock, transaction and payout history |
| `recommend-service` | 8086 | Through gateway | Search, recommendation, similar story and genre-based suggestions |
| `report-service` | 8087 | Through gateway | User reports and admin report handling |
| `ai-service` | 8088 | Through gateway | AI chat, spelling/grammar correction, writing suggestion, image generation, plagiarism check |
| `common-lib` | - | Library | Shared contracts and utility code |

`social-service` exists in the repository, but it is not enabled in the default API Gateway routes or the deploy compose stack.

### Supporting Infrastructure

| Component | Port | Purpose |
| --- | ---: | --- |
| Keycloak | 8181 local, behind Nginx in deploy | Identity and access management |
| Kafka | 9092 / 29092 | Event bus between services |
| Kafka UI | 8089 | Kafka monitoring UI |
| Zookeeper | 2181 | Kafka dependency |
| MongoDB | External | Main business data storage |
| Redis | External | Cache and fast-access data |
| OpenSearch | External | Full-text search, vector search, AI/recommendation index |
| PostgreSQL | External | Keycloak database |

## Main Technologies

- Java 21, Maven, Spring Boot, Spring Cloud, Spring Security
- Spring Cloud Gateway, Eureka, OpenFeign
- MongoDB, Redis, OpenSearch, PostgreSQL
- Kafka and Zookeeper
- Keycloak
- Cloudinary
- VNPay, MoMo
- Gemini API, Cohere, SiliconFlow, Azure Speech
- Docker, Docker Compose, Nginx
- GitHub Actions, Azure VM

## Repository Structure

```text
.
|-- .github/workflows
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
|-- docker-compose-deploy.yml
|-- nginx.conf
`-- README.md
```

## Prerequisites

Required for Docker-based setup:

- Docker
- Docker Compose v2
- Git

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

```env
# Deployment / public URLs
IP_ADDRESS=
PUBLIC_FE_URL=

# Keycloak database
DATABASE_HOST=
DATABASE_NAME=
DATABASE_USERNAME=
DATABASE_PASSWORD=

# Keycloak client / OAuth2
KEYCLOAK_ISSUER_URI=
REALM=online-stories-realm
CLIENT_ID=
CLIENT_SECRET=
KEYCLOAK_REDIRECT_URI=

# MongoDB
MONGODB_URI=
USER_MONGODB_DB=
STORY_MONGODB_DB=
TRANSACTION_MONGODB_DB=
RECOMMEND_MONGODB_DB=
REPORT_MONGODB_DB=
AI_MONGODB_DB=

# Redis / OpenSearch
REDIS_URL=
OPENSEARCH_URL=

# AI providers
GEMINI_API_KEY=
GEMINI_PROJECT_ID=
COHERE_API_KEY=
SILICON_API_KEY=
API_STORY_ENDPOINT=

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

# Optional / reserved for payout-withdrawal workflow
PAYOS_PAYOUT_CLIENT_ID=
PAYOS_PAYOUT_API_KEY=
PAYOS_PAYOUT_CHECKSUM_KEY=
PAYOS_BASE_URL=
WRITING_TOKEN_VND_RATE=100
WITHDRAW_MIN_WRITING_TOKENS=1000
```

Notes:

- `SILICON_API_KEY` is used by `ai-service` for image generation through SiliconFlow.
- `KEYCLOAK_ISSUER_URI` must match the issuer URL used by Keycloak and the frontend.
- The PayOS-related variables are reserved for the payout/withdrawal workflow. The main active payment flow currently uses VNPay and MoMo.

## Running Locally

### 1. Start local infrastructure

Use this mode when developing services directly from the IDE or terminal. It starts Keycloak, Kafka, Zookeeper, and Kafka UI.

```bash
docker compose up -d
```

Useful local URLs:

- Keycloak: http://localhost:8181
- Kafka UI: http://localhost:8089

Stop local infrastructure:

```bash
docker compose down
```

### 2. Run a service manually

Start local infrastructure first, then install the shared library:

```bash
cd common-lib
mvn clean install -DskipTests
```

Run a service:

```bash
cd ../story-service
mvn spring-boot:run
```

Repeat with any service directory as needed. When running a service outside Docker, make sure its environment variables point to the local infrastructure services.

## Running the Full Backend Stack

Use the deploy compose file to build and run all backend services:

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
docker compose -f docker-compose-deploy.yml logs -f nginx
```

Stop the full stack:

```bash
docker compose -f docker-compose-deploy.yml down
```

In the deploy stack, public traffic goes through Nginx:

- Backend APIs: `http://novel-tool-kit.id.vn/api/...`
- Keycloak UI: `http://novel-tool-kit.id.vn/`

If TLS is configured by an upstream proxy or DNS provider, use the equivalent `https://` URLs.

## API Gateway Routes

All backend APIs are routed by `api-gateway`.

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

## AI Features

`ai-service` currently provides these main capabilities through `/api/ai/**`:

- AI chat and writing assistance.
- Spelling correction.
- Grammar improvement.
- Content suggestion.
- Image generation with SiliconFlow `FLUX.1-schnell`.
- Plagiarism check using internal vector search and LLM-based external analysis.

The service uses Gemini through Spring AI for chat/embedding tasks. The configured models are:

- Chat model: `gemini-3.1-flash-lite`
- Embedding model: `gemini-embedding-001`
- OpenSearch vector index: `story_chunks_vector`

## Event Topics

Shared Kafka topics are defined in `common-lib`.

| Topic | Purpose |
| --- | --- |
| `story.updated.v1` | Sync story data to search/recommendation services |
| `story.metrics-updated.v1` | Sync story metrics |
| `payout.processed.v1` | Notify payout processing result |
| `chapter.published.v1` | Chapter published event |
| `chapter.published.v2` | Chapter approved/published event |
| `chapter.taken-down.v1` | Chapter taken down event |
| `chapter.sync.v1` | Sync chapter content to AI/vector store |
| `report.responded.v1` | Report response notification |
| `user.created.v1` | Sync new user data |
| `user.updated.v1` | Sync updated user data |
| `transaction.event.v1` | Transaction event |

## CI/CD Deployment

The repository includes a GitHub Actions workflow at `.github/workflows/deploy.yml`.

On every push to `master`, the workflow:

1. Connects to the Azure VM through SSH.
2. Pulls the latest code in the `NovelToolKit` directory.
3. Rebuilds and restarts the stack with `docker-compose-deploy.yml`.
4. Prunes unused Docker images.

Required GitHub secrets:

- `SERVER_IP`
- `SERVER_USERNAME`
- `SSH_PRIVATE_KEY`

## Useful Commands

Rebuild selected services:

```bash
docker compose -f docker-compose-deploy.yml build story-service api-gateway
docker compose -f docker-compose-deploy.yml up -d story-service api-gateway
```

Restart a service:

```bash
docker compose -f docker-compose-deploy.yml restart story-service
```

Check service registration:

- Eureka dashboard: http://localhost:8761

Check Kafka topics and messages:

- Kafka UI: http://localhost:8089

## Troubleshooting Notes

- If the gateway returns `401` or token validation fails, check `KEYCLOAK_ISSUER_URI` and the Keycloak hostname configuration.
- If services cannot discover each other, check Eureka at port `8761` and each service's `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`.
- If Kafka consumers do not receive events, check `SPRING_KAFKA_BOOTSTRAP_SERVERS` and Kafka UI.
- If AI features fail, check `GEMINI_API_KEY`, `GEMINI_PROJECT_ID`, `OPENSEARCH_URL`, and `SILICON_API_KEY`.
- If file upload fails, check Cloudinary credentials.
- If deployed APIs are unreachable, check `nginx-proxy` logs and `nginx.conf`.
