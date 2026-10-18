# DMS
Document Management System for archiving, OCR processing, full-text search, and automatic document summarization.

GitHub repository: [Hashkeil/DMS](https://github.com/Hashkeil/DMS)

## Architecture

```
Browser --> paperless-ui (nginx, port 80) --/api/--> paperless-rest --> postgres
                                                      |        --> PDF volume
                                                      +--> RabbitMQ --> empty OCR worker
```

| Service          | Description                                                               | Host port |
|------------------|---------------------------------------------------------------------------|-----------|
| `paperless-ui`   | Angular frontend served by nginx; reverse-proxies `/api/` to the backend  | 80        |
| `paperless-rest` | REST API (Spring Boot) for documents and categories                       | internal  |
| `postgres`       | PostgreSQL database                                                       | 5432      |
| `rabbitmq` | Durable OCR queue and management UI | 5672 / 15672 (localhost only) |
| `paperless-ocr-worker` | Validates and logs uploaded-document messages | internal |

The REST service is only reachable through nginx, so the browser talks to a single origin and no CORS config is needed.

## Run with Docker

```sh
docker compose up --build
```

- UI (dashboard): http://localhost/
- REST API through nginx: http://localhost/api/documents, http://localhost/api/categories

## UI

- `/` – dashboard listing all documents
- `/documents/new` – create a document
- `/documents/{id}` – detail page to view, edit, and delete a document

Input is validated in the browser with Angular Reactive Forms (title and filename required, not blank, max. 255 characters),
mirroring the Bean Validation rules of the backend. The backend stays authoritative; its error messages are shown in the UI.

### Local frontend development

Start the Docker stack (API available through nginx on port 80), then:

```sh
cd paperless-ui
npm install
npm start
```

Open http://localhost:4200/. `proxy.conf.json` forwards `/api` to `http://localhost:80`.

## Tests

Run all backend tests:

```sh
mvn clean test
```

## CI and Security Checks

- **Build and Test** runs `mvn clean test` with Temurin Java 25 to check the Maven build and all tests.
- **CodeQL Security Scan** builds with `mvn clean package -DskipTests`, then scans Java/Kotlin code for security/CWE-style weaknesses using `security-extended` and `security-and-quality` queries.
- **Dependency Review** checks dependency changes in pull requests and fails for newly introduced vulnerabilities rated moderate or higher.

Build and Test and CodeQL run on pull requests targeting `master` and pushes to `master`. CodeQL also runs every Monday at 06:23 UTC. Dependency Review runs only on pull requests targeting `master`.

## PDF upload and queues

The stack now includes RabbitMQ and a separate empty OCR worker. Upload a PDF from the document creation form or POST multipart data to `/api/documents/upload` with `file`, `title`, optional `description` and `categoryId`. Existing JSON metadata CRUD remains supported; it does not enqueue OCR work.

PDFs (maximum 10 MiB) are stored in the `uploads` Docker volume. PostgreSQL stores metadata and the generated storage key. RabbitMQ receives a persistent JSON event; the worker logs the document ID and filename. Actual OCR is deferred to Sprint 4.

`docker compose build` builds and tests both Java modules and the frontend. Start with `docker compose up -d`, open http://localhost/, and inspect `docker compose logs -f paperless-ocr-worker`. The management UI is at http://localhost:15672 (local defaults: paperless / paperless). Override `RABBITMQ_USER` and `RABBITMQ_PASSWORD` as needed; existing broker volumes retain their initialized users.

Run Java tests with JDK 25: `mvn clean test`. Run frontend tests: `npm --prefix paperless-ui test`; build: `npm --prefix paperless-ui run build`. Frontend tests currently cover the pure upload-validation helper, not full browser interactions.

For Java development outside Docker, start `docker compose up -d postgres rabbitmq`; the Java process uses ports 5432 and 5672 on localhost. The Angular proxy currently targets the Docker nginx entry point; change its target to localhost:8081 only when running Java directly.

