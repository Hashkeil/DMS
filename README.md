# DMS
Document Management System for archiving, OCR processing, full-text search, and automatic document summarization.

GitHub repository: [Hashkeil/DMS](https://github.com/Hashkeil/DMS)

## Architecture

```
Browser --> paperless-ui (nginx, port 80) --/api/--> paperless-rest (Spring Boot, 8081, internal) --> postgres
```

| Service          | Description                                                               | Host port |
|------------------|---------------------------------------------------------------------------|-----------|
| `paperless-ui`   | Angular frontend served by nginx; reverse-proxies `/api/` to the backend  | 80        |
| `paperless-rest` | REST API (Spring Boot) for documents and categories                       | internal  |
| `postgres`       | PostgreSQL database                                                       | 5432      |

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

Start the backend locally on port 8081, then:

```sh
cd paperless-ui
npm install
npm start
```

Open http://localhost:4200/. `proxy.conf.json` forwards `/api` to `http://localhost:8081`.

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
