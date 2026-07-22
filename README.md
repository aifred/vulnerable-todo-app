# vulnerable-todo-app

A small Java/Spring Boot to-do list REST API that **intentionally contains a
set of well-known vulnerability classes**, built as a test fixture for
calibrating static analysis rules (SonarQube) and CI security gates.

> **Do not deploy this application to any network-reachable environment, and
> do not reuse any code from it in a real project.** It is deliberately
> insecure. Run it only on a local machine or in an isolated CI job.

## Why this exists

Security tooling (SAST rules, CI quality gates, training material) needs
known-bad code to validate against. This repo provides a small, readable
Spring Boot app where each vulnerability is isolated, commented with its CWE
ID, and easy to map back to a specific SonarQube rule.

## Vulnerabilities included

| # | Vulnerability | CWE | Location |
|---|---|---|---|
| 1 | SQL injection via string concatenation (login) | CWE-89 | `repository/UserRepository.java#findByCredentialsUnsafe` |
| 2 | SQL injection via string concatenation (search) | CWE-89 | `repository/TodoRepository.java#searchUnsafe` |
| 3 | Weak/unsalted password hash (MD5) | CWE-327 / CWE-916 | `util/CryptoUtil.java#hashPassword` |
| 4 | Hardcoded application secret | CWE-798 | `util/CryptoUtil.java`, `application.properties` |
| 5 | Hardcoded datasource credentials | CWE-798 / CWE-259 | `application.properties` |
| 6 | Insecure randomness for session tokens | CWE-330 | `util/CryptoUtil.java#generateSessionToken` |
| 7 | Sensitive data (passwords) written to logs | CWE-532 | `controller/AuthController.java` |
| 8 | Broken object-level authorization / IDOR | CWE-639 | `controller/TodoController.java#getById,update,delete` |
| 9 | Reflected XSS (raw HTML response) | CWE-79 | `controller/TodoController.java#share` |
| 10 | Reflected/stored XSS (unescaped Thymeleaf `th:utext`) | CWE-79 | `templates/greeting.html` |
| 11 | Path traversal on upload | CWE-22 | `controller/FileController.java#upload` |
| 12 | Path traversal on download | CWE-22 | `controller/FileController.java#download` |
| 13 | OS command injection | CWE-78 | `controller/FileController.java#preview` |
| 14 | XML External Entity (XXE) injection | CWE-611 | `controller/ImportController.java#importXml` |
| 15 | Insecure deserialization of untrusted data | CWE-502 | `controller/BackupController.java#restore` |
| 16 | CSRF protection disabled + all routes permitted | CWE-352 | `config/SecurityConfig.java` |
| 17 | Overly permissive CORS (wildcard origin) | CWE-942 | `config/CorsConfig.java` |
| 18 | Stack traces / internal errors returned to clients | CWE-209 | `exception/GlobalExceptionHandler.java`, `application.properties` |
| 19 | Admin/debug surfaces exposed (H2 console, all actuator endpoints) | CWE-16 | `application.properties` |

Each site in the code carries a Javadoc/comment block explaining the exact
attack and the CWE it maps to, so a reviewer (human or scanner) can trace a
SonarQube finding straight back to the intent.

## Running locally

Requires JDK 17+ and Maven.

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080` with an in-memory H2 database that
is reset on every restart. No data persists between runs.

## Running tests

```bash
mvn test
```

## Static analysis with SonarQube

### 1. Run a SonarQube server

For local experimentation, run SonarQube Community/Developer Edition via
Docker:

```bash
docker run -d --name sonarqube -p 9000:9000 sonarqube:community
```

Log in at `http://localhost:9000` (default `admin`/`admin`, you'll be
prompted to change it), create a project, and generate a project analysis
token under **My Account → Security → Generate Tokens**.

### 2. Analyze locally

```bash
mvn clean verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=<your-token>
```

### 3. CI

`.github/workflows/build.yml` only builds the project and runs tests with
JaCoCo coverage — it does not call SonarQube. Run the `sonar:sonar` command
above locally (or wire it into CI yourself) against whichever SonarQube
server/token you're using.

Given the density of intentional findings in this repo, expect the default
Quality Gate to fail the first analysis if you do run it — that's the point:
it demonstrates the gate actually blocking a real (if intentionally seeded)
set of security issues.

## Project layout

```
src/main/java/com/example/todoapp/
├── TodoAppApplication.java     # Spring Boot entry point
├── controller/                 # REST + MVC endpoints (most vulnerabilities live here)
├── repository/                 # JDBC data access (SQL injection sinks)
├── util/                       # Weak crypto / token generation
├── config/                     # Security & CORS configuration
└── exception/                  # Global error handling
```
