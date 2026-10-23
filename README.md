# AuthGuard

Centralized authorization service. Keycloak (OIDC) handles **authentication** and
issues JWTs; AuthGuard makes **authorization** decisions (RBAC today, ABAC later)
on top of the identity a verified JWT carries.

Status: early scaffold. See `docs/plan.md` (to be added) for the staged roadmap.

## Prerequisites

- JDK 21
- Maven 3.9+
- Docker Desktop (for PostgreSQL, and later Keycloak)

## Running locally

1. Start PostgreSQL:
   ```bash
   docker compose up -d
   ```
2. Run the app:
   ```bash
   mvn spring-boot:run
   ```
3. Check it's alive:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

## Running tests

```bash
mvn test
```
