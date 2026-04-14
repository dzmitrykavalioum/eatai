# CLAUDE.md — EatAI Developer Guide

## Project Summary

EatAI is a Kotlin Multiplatform (KMP) application for discovering nearby cafes and restaurants.
See [SPEC.md](SPEC.md) for full product requirements.

**Active targets:** Android, Web (Compose for Web / Wasm)
**Future:** iOS, backend API

---

## Tech Stack

### Active (MVP)

| Concern              | Library / Tool                                      |
|----------------------|-----------------------------------------------------|
| Language             | Kotlin (KMP)                                        |
| Shared UI            | Compose Multiplatform                               |
| Android maps         | MapLibre Android SDK                                |
| Web maps             | MapLibre GL JS (via Kotlin/JS interop)              |
| Networking           | Ktor Client (CIO engine)                            |
| Serialization        | Kotlinx Serialization (JSON)                        |
| Dependency injection | Koin (KMP-compatible)                               |
| Local DB             | SQLDelight (KMP)                                    |
| Preferences          | multiplatform-settings (SharedPrefs / NSUserDefaults / localStorage) |
| Async                | Coroutines + Flow                                   |
| Venue data           | OpenStreetMap Overpass API (free, no API key)       |
| Geocoding            | Nominatim API (OSM, free)                           |
| Build system         | Gradle with Kotlin DSL                              |

### Planned / Future

| Feature                   | Technology                                        |
|---------------------------|---------------------------------------------------|
| Backend server            | Ktor Server (module: `/backend`)                  |
| Auth                      | JWT + SSO (Google Sign-In, Apple Sign-In)         |
| Maps (post-MVP)           | Google Maps SDK (Android / iOS / JS)              |
| Venue data (post-MVP)     | Google Places API                                 |
| Ratings                   | Google Places API                                 |
| iOS                       | Compose Multiplatform or SwiftUI                  |
| CI/CD                     | GitHub Actions (`.github/workflows/`)             |
| Push notifications        | FCM (Android), APNs (iOS)                         |

---

## Module Structure

```
eatai/
├── composeApp/                  # Compose Multiplatform — UI layer
│   ├── commonMain/
│   │   ├── screens/             # MapScreen, ListScreen, DetailScreen, PreferencesScreen
│   │   ├── components/          # Shared composables (markers, cards, bottom sheets)
│   │   ├── viewmodel/           # ViewModels (MapViewModel, SearchViewModel, etc.)
│   │   └── navigation/          # NavGraph
│   ├── androidMain/
│   │   └── map/                 # MapLibre Android SDK integration (expect/actual)
│   └── wasmJsMain/
│       └── map/                 # MapLibre GL JS interop (expect/actual)
│
├── shared/                      # Pure KMP — business logic, no UI
│   ├── commonMain/
│   │   ├── data/
│   │   │   ├── api/             # Overpass API client, Nominatim client
│   │   │   ├── db/              # SQLDelight schema and queries
│   │   │   ├── dto/             # API response models
│   │   │   └── repository/      # VenueRepository, FavoritesRepository
│   │   ├── domain/
│   │   │   ├── model/           # Venue, OpeningHours, MarkerState, Filter
│   │   │   └── usecase/         # GetNearbyVenuesUseCase, GetVenueDetailUseCase, etc.
│   │   └── platform/            # expect declarations (location, permissions)
│   ├── androidMain/             # Android actual implementations
│   ├── iosMain/                 # iOS (future)
│   └── wasmJsMain/              # Web actual implementations
│
├── backend/                     # Placeholder — future Ktor server
│   └── README.md                # Planned backend architecture notes
│
├── SPEC.md
└── CLAUDE.md
```

---

## Architecture

**Pattern:** MVVM + Kotlin Flow

```
UI (Composable)
    └── ViewModel (StateFlow)
            └── UseCase
                    └── Repository
                            └── API Client / SQLDelight
```

- All state is `StateFlow<ScreenState>` — no `LiveData`
- Side effects (navigation, snackbars) are sent via `SharedFlow`
- ViewModels live in `composeApp/commonMain`
- Use cases and repositories live in `shared/commonMain`
- No business logic in composables

---

## Maps Strategy

Map integration is platform-specific by necessity — no single KMP-native map library covers Android + Web + iOS today. We use the **MapLibre ecosystem** for consistency (same rendering engine and tile system across platforms):

| Platform | SDK                  |
|----------|----------------------|
| Android  | MapLibre Android SDK |
| Web      | MapLibre GL JS       |
| iOS      | MapLibre iOS SDK (future) |

Each platform provides its own `actual MapView` composable, with the shared `expect MapView` interface in `composeApp/commonMain`. All marker state and interaction logic is shared.

> **Migration note:** When a Google API key is available, replace MapLibre with Google Maps SDK (Android), Maps JS API (Web), and Google Maps SDK (iOS). Data source switches from Overpass API to Google Places API at the same time.

---

## Overpass API — Query Template

Used to fetch nearby venues in MVP:

```
[out:json];
(
  node["amenity"~"cafe|restaurant|bar|bakery"](around:{radius_meters},{lat},{lon});
  way["amenity"~"cafe|restaurant|bar|bakery"](around:{radius_meters},{lat},{lon});
);
out center tags;
```

Endpoint: `https://overpass-api.de/api/interpreter`
Method: POST, body = query string above.
No API key required. Rate limit: be conservative, cache results.

---

## Marker Color Logic

Computed from OSM `opening_hours` tag after parsing:

| Condition                          | MarkerState | Color  |
|------------------------------------|-------------|--------|
| Open, closes in > 2h              | OPEN        | Green  |
| Open, closes in 1h – 2h           | CLOSING_SOON| Yellow |
| Open, closes in < 1h              | CLOSING     | Orange |
| Open, closes in < 15 min          | LAST_CALL   | Red    |
| Closed or hours unknown            | CLOSED      | Grey   |

`MarkerState` enum lives in `shared/commonMain/domain/model/`.

---

## Preferences

Stored via `multiplatform-settings`:

| Key                    | Type        | Default   |
|------------------------|-------------|-----------|
| `default_radius_km`    | Float       | 2.0       |
| `preferred_cuisines`   | String (JSON list) | `[]` |
| `favorites` (IDs)      | String (JSON list) | `[]` |

Favorites data (full venue objects) are stored in SQLDelight.

---

## Key Development Rules

- All business logic lives in `shared/commonMain` — zero platform code in use cases or repositories
- `expect/actual` is used only for: maps, location provider, permissions, platform preferences
- No hardcoded API endpoints — use a `constants` file in `shared/commonMain/data/api/`
- All network calls go through the repository layer — composables never call APIs directly
- Use `StateFlow`, not `LiveData` or `MutableState` directly in ViewModels
- Avoid adding error handling for scenarios that cannot happen at the current layer
- Do not add features or abstractions beyond what SPEC.md describes

---

## Future Feature Notes

These are NOT MVP tasks. Noted here to avoid conflicting architectural decisions:

- **Backend (`/backend` module):** Ktor server. Routes: POST /auth/register, POST /auth/login, POST /auth/logout, DELETE /auth/account. Will also sync favorites and preferences per user.
- **SSO:** Google Sign-In (Android/Web), Apple Sign-In (iOS/Web). Implement after backend auth is ready.
- **Google Maps migration:** Swap MapLibre for Google Maps SDK. Requires Google API key. Also enables Google Places for richer data and real ratings.
- **iOS:** Add `iosMain` targets to both `shared` and `composeApp`. Add MapLibre iOS SDK actual implementation.
- **CI/CD:** GitHub Actions. Jobs: `build-android`, `build-web`, `test-shared`, `lint`.
- **Dark mode:** Add `ThemeViewModel`, persist choice in `multiplatform-settings`. Use MaterialTheme dynamic colors.
- **Push notifications:** FCM for Android, APNs for iOS. Backend sends triggers (e.g. "venue closing soon" for saved favorites).
