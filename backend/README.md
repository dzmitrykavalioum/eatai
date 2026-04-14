# Backend (Placeholder)

This module will contain the EatAI server-side API once user accounts are needed.

## Planned Stack

- **Framework:** Ktor Server (Kotlin)
- **Auth:** JWT tokens + SSO (Google Sign-In, Apple Sign-In)
- **Database:** PostgreSQL (via Exposed ORM)

## Planned Routes

| Method | Path                   | Description                    |
|--------|------------------------|--------------------------------|
| POST   | /auth/register         | Create account (email + password) |
| POST   | /auth/login            | Login, returns JWT             |
| POST   | /auth/logout           | Invalidate session             |
| DELETE | /auth/account          | Delete account and all data    |
| GET    | /favorites             | List user's saved venues        |
| POST   | /favorites             | Add a venue to favorites        |
| DELETE | /favorites/{id}        | Remove a favorite               |
| GET    | /preferences           | Get user preferences            |
| PUT    | /preferences           | Update user preferences         |

## Setup (when ready)

```
cd backend
./gradlew run
```

Default port: `8080`

## Notes

- The mobile app and web app currently operate fully offline/local — no backend calls are made in MVP.
- When this module is activated, `include(":backend")` in `settings.gradle.kts` must be uncommented.
