# DMS
Document Management System for archiving, OCR processing, full-text search, and automatic document summarization.

GitHub repository: [Hashkeil/DMS](https://github.com/Hashkeil/DMS)

Run all tests:

```sh
mvn clean test
```

## CI and Security Checks

- **Build and Test** runs `mvn clean test` with Temurin Java 25 to check the Maven build and all tests.
- **CodeQL Security Scan** builds with `mvn clean package -DskipTests`, then scans Java/Kotlin code for security/CWE-style weaknesses using `security-extended` and `security-and-quality` queries.
- **Dependency Review** checks dependency changes in pull requests and fails for newly introduced vulnerabilities rated moderate or higher.

Build and Test and CodeQL run on pull requests targeting `main` and pushes to `main`. CodeQL also runs every Monday at 06:23 UTC. Dependency Review runs only on pull requests targeting `main`. 