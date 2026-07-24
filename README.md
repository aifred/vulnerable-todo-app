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

## Code smells: the profile feature

The `profile/*` code (`model/Profile{Entity,Dto,VO}.java`,
`repository/ProfileRepository.java`, `service/Profile*.java`,
`controller/ProfileController.java`, `templates/profile.html`) isn't about a
security CWE — it's a showcase of maintainability/code-smell anti-patterns
for calibrating SonarQube's reliability and maintainability rules (and
"Cognitive Complexity" in particular). It still compiles, boots, and works;
that's the point — this is what shipped, reviewed, "AI slop" looks like in
practice, not code that fails to run.

| # | Smell | Where |
|---|---|---|
| 1 | Three near-identical model classes (`ProfileEntity`/`ProfileDto`/`ProfileVO`) with drifted field names (`avatarUrl` vs `avatar`) | `model/Profile*.java` |
| 2 | Four near-duplicate repository lookup methods implementing the same query | `repository/ProfileRepository.java#getProfileById,getProfileByID,fetchProfileData,retrieveUserProfileInformationRecord` |
| 3 | Cargo-cult `Thread.sleep` "fix" for a race that was never diagnosed, left in place after the cache it guarded was fixed | `repository/ProfileRepository.java#waitForCacheToSettle` |
| 4 | Interface + abstract base class + single implementation + unused factory, for one concrete service | `service/ProfileService.java`, `AbstractProfileServiceBase.java`, `ProfileServiceImpl.java`, `ProfileServiceFactory.java` |
| 5 | Copy-pasted validation logic repeated per method, each copy subtly different (one drops a null check) | `service/ProfileServiceImpl.java#updateBio,updateAvatar,updateFavoriteColor` |
| 6 | String-based if/else dispatcher instead of polymorphism for routing one of five actions | `service/ProfileServiceImpl.java#doProfileStuff` |
| 7 | `println` debug logging of raw request bodies (which may contain the same fields other endpoints treat as sensitive) | `controller/ProfileController.java`, `service/ProfileServiceImpl.java` |
| 8 | Dead code: an unused factory class, three interface methods that only ever throw `UnsupportedOperationException` | `ProfileServiceFactory.java`, `AbstractProfileServiceBase.java` |
| 9 | Constants-in-an-interface anti-pattern with meaningless magic numbers | `util/ProfileConstants.java` |
| 10 | "Logger" that returns a meaningless boolean and grows an in-memory list forever | `util/ProfileActivityLogger.java` |
| 11 | Duplicated view/edit HTML blocks instead of one templated form; inline `!important` CSS; inline JS built via string concatenation | `templates/profile.html` |

A few smells from the original version of this feature were fixed after an automated PR review caught them (see PR #1 history): the mapper dropping `avatarUrl`, a client-controlled `admin`/`skipValidation` query parameter that bypassed all validation, empty catch blocks that swallowed exceptions while still reporting success, a SQL-injectable duplicate lookup method, and a cache stored as a mutable `static` field written from instance methods. Those were genuine bugs/vulnerabilities rather than pure maintainability smells, so they were fixed rather than preserved.

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

`.github/workflows/build.yml` builds the project, runs tests with JaCoCo
coverage, and then analyzes the result against
[SonarQube Cloud](https://sonarcloud.io) (organization `aifred`), waiting on
and enforcing the Quality Gate. This runs automatically on every push and
pull request targeting `main`.

To enable it on a fork or a new clone of this repo, add one repository
secret under **Settings → Secrets and variables → Actions → Secrets**:

- `SONAR_TOKEN` — a SonarQube Cloud analysis token for the `aifred`
  organization (generate one under **My Account → Security** on
  sonarcloud.io).

Without that secret, the `SonarQube analysis` step will fail authentication.
The local Docker workflow above (steps 1–2) remains available for
experimenting against a self-hosted server without touching CI at all.

Given the density of intentional findings in this repo, expect the default
Quality Gate to fail analysis — that's the point: it demonstrates the gate
actually blocking a real (if intentionally seeded) set of security issues.

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
