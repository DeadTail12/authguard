# Keycloak setup

Keycloak handles **authentication** (who you are); AuthGuard handles **authorization**
(what you're allowed to do). Keycloak never stores AuthGuard's roles/permissions —
that model lives entirely in AuthGuard's own PostgreSQL database, keyed by the
Keycloak user id (`sub` claim). This keeps one single source of truth for permissions
instead of two systems that could drift apart.

## What gets created automatically

`docker compose up -d` starts Keycloak in dev mode and imports
[`keycloak/realm-export.json`](../keycloak/realm-export.json) on boot:

- **Realm:** `authguard`
- **Client:** `authguard-api` — public client, direct access grants enabled (so we can
  fetch tokens straight from the command line while testing, without a browser login).
- **Test users:** `alice` / `alice123`, `bob` / `bob123`. No roles — see above.

Admin console: http://localhost:8081 — login `admin` / `admin`.

## Getting a token

```bash
curl -s -X POST "http://localhost:8081/realms/authguard/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=authguard-api" \
  -d "grant_type=password" \
  -d "username=alice" \
  -d "password=alice123" | jq -r .access_token
```

## Calling the API

```bash
TOKEN="<paste the access_token from above>"
curl -s http://localhost:8080/api/v1/me -H "Authorization: Bearer $TOKEN"
```

Expected: `{"userId":"<alice's Keycloak sub>","username":"alice","email":"alice@example.com"}`.

Without the header, or with a garbage token, the same request returns `401 Unauthorized` —
Spring Security rejects it before it ever reaches `MeController`.
