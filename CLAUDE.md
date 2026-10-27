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
| Android maps         | Google Maps SDK for Android (Maps Compose)          |
| Web maps             | Google Maps JS API (loaded via `<script>` in index.html) |
| Networking           | Ktor Client (CIO engine)                            |
| Serialization        | Kotlinx Serialization (JSON)                        |
| Dependency injection | Koin (KMP-compatible)                               |
| Local DB             | SQLDelight (KMP)                                    |
| Preferences          | multiplatform-settings (SharedPrefs / NSUserDefaults / localStorage) |
| Async                | Coroutines + Flow                                   |
| Venue data           | Google Places API (New) — Nearby Search, same API key as Google Maps |
| Geocoding            | Nominatim API (OSM, free)                           |
| Build system         | Gradle with Kotlin DSL                              |

### Planned / Future

| Feature                   | Technology                                        |
|---------------------------|---------------------------------------------------|
| Backend server            | Ktor Server (module: `/backend`)                  |
| Auth                      | JWT + SSO (Google Sign-In, Apple Sign-In)         |
| Maps (iOS)                | Google Maps SDK for iOS                           |
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
│   │   └── map/                 # Google Maps SDK integration (expect/actual)
│   └── wasmJsMain/
│       └── map/                 # Google Maps JS API interop (expect/actual)
│
├── shared/                      # Pure KMP — business logic, no UI
│   ├── commonMain/
│   │   ├── data/
│   │   │   ├── api/             # Google Places API client, Nominatim client
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

Map integration is platform-specific by necessity — no single KMP-native map library covers Android + Web + iOS today. We use the **Google Maps** SDKs on every platform for consistency (same rendering engine and tile system, same API key as Places):

| Platform | SDK                  |
|----------|----------------------|
| Android  | Google Maps SDK for Android |
| Web      | Google Maps JS API   |
| iOS      | Google Maps SDK for iOS (future) |

Each platform provides its own `actual MapView` composable, with the shared `expect MapView` interface in `composeApp/commonMain`. All marker state and interaction logic is shared.

---

## Google Places API (New) — Nearby Search

Used to fetch nearby venues:

```
POST https://places.googleapis.com/v1/places:searchNearby
X-Goog-Api-Key: {MAPS_API_KEY}
X-Goog-FieldMask: places.id,places.displayName,places.location,places.formattedAddress,places.types,places.rating,places.currentOpeningHours,places.nationalPhoneNumber,places.websiteUri
Content-Type: application/json

{
  "includedTypes": ["cafe", "restaurant", "bar", "bakery", "fast_food_restaurant"],
  "maxResultCount": 20,
  "locationRestriction": {
    "circle": { "center": { "latitude": {lat}, "longitude": {lon} }, "radius": {radius_meters} }
  }
}
```

- **API key:** the same key as Google Maps (`MAPS_API_KEY` in `local.properties`, gitignored). *Places API (New)* must be enabled for it in Google Cloud Console, and the key's application restrictions must allow the Android package(s) and SHA-1 fingerprints. Never hardcode or commit the key.
- **Field mask is required** and drives billing — request only the fields the UI uses. `rating` and `currentOpeningHours` are billed at a higher SKU than basic fields.
- **Max 20 results per request** (no pagination on Nearby Search). Radius max is 50 000 m.
- Endpoint URL, field mask and cache limits live in `shared/commonMain/data/api/Constants.kt`.
- **Key delivery:** `shared` gets the key through `PlacesConfig`, provided by each platform entry point — Android from `BuildConfig.MAPS_API_KEY` (also sends `X-Android-Package` / `X-Android-Cert`, which an Android-restricted key requires for REST calls), web from `window.__EATAI_MAPS_KEY` injected into `index.html`.

### Request budget (every call is billed)

- **Cache** (`NearbyPlacesCache`, in `VenueRepository`): reused for the same radius and a search centre within 100 m of a cached one; 15 min TTL, 20 entries, LRU eviction. It stores raw results, so marker colour and today's hours are recomputed on every read. The cache key is the area only — filters (search text, categories) are applied client-side and never trigger a refetch.
- **No retries, no loops.** `PlacesApiClient` never retries; errors go to the UI. A search starts only from a user action (Search button, keyboard Search, Apply on Filters) or once after the first location fix. Nothing may trigger a search from a state change, camera move or `LaunchedEffect`.
- `MapViewModel` keeps one search job: a new search cancels the previous one. Concurrent repository calls are serialised, so overlapping searches for one area share a single request.

---

## Marker Color Logic

Computed from the Places `currentOpeningHours` field (`openNow`, `periods`, `nextCloseTime`):

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
- **iOS:** Add `iosMain` targets to both `shared` and `composeApp`. Add Google Maps SDK for iOS actual implementation.
- **CI/CD:** GitHub Actions. Jobs: `build-android`, `build-web`, `test-shared`, `lint`.
- **Dark mode:** Add `ThemeViewModel`, persist choice in `multiplatform-settings`. Use MaterialTheme dynamic colors.
- **Push notifications:** FCM for Android, APNs for iOS. Backend sends triggers (e.g. "venue closing soon" for saved favorites).
