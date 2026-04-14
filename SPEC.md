# EatAI — Product Specification

## Overview

EatAI is a cross-platform application that helps users discover nearby cafes and restaurants in real time. The core experience is a map screen showing venues filtered by user preferences: cuisine type, current availability, and time until closing. The project starts as Android + Web, with iOS to follow.

---

## Target Platforms

| Phase  | Platform                                  |
|--------|-------------------------------------------|
| MVP    | Android                                   |
| MVP    | Web (Compose for Web / Wasm)              |
| Future | iOS                                       |

---

## Screens & Features

### 1. Map Screen (Launch Screen)

- Opens on launch, centered on the user's current location
- Default: shows **all** nearby cafes/restaurants (no filter applied)
- Toggle between **Map Mode** and **List Mode** via a top-right button
- "Search" button triggers a fresh query with current filters applied

---

### 2. Filter / Conditions Panel

Accessible as a bottom sheet or collapsible panel above the search button.

| Filter            | Options                                                                 |
|-------------------|-------------------------------------------------------------------------|
| Food category     | Coffee, Breakfast, Lunch, Dinner, Desserts, Vegan, Bakery, Fast Food   |
| Free-text search  | Additional text input for custom queries                                |
| Availability      | Open now / Open in next 1h / Open in next 2h / Open in next 3h         |
| Search radius     | Slider: 0.5 km – 10 km (default: 2 km)                                 |

If no filters are selected, all nearby venues are shown.

---

### 3. Map Mode

- Color-coded markers based on remaining open time:

| Marker Color | Meaning                      |
|--------------|------------------------------|
| Green        | More than 2 hours open       |
| Yellow       | 1 – 2 hours remaining        |
| Orange       | Less than 1 hour remaining   |
| Red          | Last 15 minutes open         |
| Grey         | Currently closed             |

- Tapping a marker opens the **Cafe Detail** panel

---

### 4. List Mode

- Venues sorted by distance (closest first)
- Each row shows:
  - Name
  - Distance from user
  - Cuisine tags
  - Open / closing time (e.g. "Closes at 22:00")
  - Color-coded status badge (matches marker colors)
- Tapping a row opens the **Cafe Detail** panel

---

### 5. Cafe Detail Panel

Displayed as a bottom sheet or full screen. Contains:

- Name
- Address
- Opening hours (today's schedule)
- Distance from user
- Phone number (if available in OSM data)
- Menu link (if available in OSM data)
- Rating — placeholder ("N/A") in MVP; Google Rating in a future update
- **"Get Directions"** button — opens external maps app (Google Maps / Apple Maps / OsmAnd)
- **"Save to Favorites"** toggle (local, no account required)

---

### 6. Favorites

- Saved locally on-device (no account required in MVP)
- Viewable from main screen or preferences
- Persisted via SQLDelight (KMP database)

---

### 7. Preferences Screen

| Setting               | MVP Status          |
|-----------------------|---------------------|
| Default search radius | Implemented         |
| Preferred cuisines    | Implemented         |
| Clear favorites       | Implemented         |
| Notification settings | Placeholder (future)|
| Dark mode toggle      | Placeholder (future)|
| Account (login/logout)| Placeholder (future)|

---

## Data Sources — MVP

| Data                  | Source                                         |
|-----------------------|------------------------------------------------|
| Venue data            | OpenStreetMap via Overpass API (free, no key)  |
| Opening hours         | OSM tags (`opening_hours`)                     |
| Ratings               | Not available in MVP                           |
| Geocoding             | Nominatim (OSM, free, no key)                  |

> **Migration plan:** When a Google API key is available, migrate to Google Places API for richer data and real ratings. This is a planned task, not MVP scope.

---

## Future Features

| Feature                         | Notes                                            |
|---------------------------------|--------------------------------------------------|
| Backend service                 | Ktor server in `/backend` module                 |
| User accounts (register / login / logout / delete) | JWT-based auth           |
| Google Sign-In / Apple Sign-In  | SSO after accounts are implemented               |
| Google Maps + Google Places     | Richer venue data and ratings                    |
| iOS app                         | Compose Multiplatform or SwiftUI                 |
| Dark mode                       | MaterialTheme dynamic colors                     |
| Push notifications              | FCM (Android), APNs (iOS)                        |
| CI/CD                           | GitHub Actions (build, lint, test)               |
| Search history                  | Recent searches saved locally                    |
| Offline mode                    | Full venue caching for offline browsing          |

---

## Out of Scope (MVP)

- User accounts / authentication
- Backend API
- Reviews or ratings
- Push notifications
- Full offline mode
- Dark mode UI
- iOS build
