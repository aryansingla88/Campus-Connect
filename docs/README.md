# CampusConnect (Phase 2)  -- Working Guidelines

## Overview

CampusConnect is being rebuilt using **Kotlin + Jetpack Compose** with a **feature-based architecture**.

Phase 1 (Java/XML) is preserved under `phase1/` for reference and gradual migration.

---

## Project Structure

```
com.example.campusconnect

├── phase1/        → Old Java/XML code (DO NOT MODIFY)
├── core/          → Navigation, theme, shared UI
├── model/         → Global data models (controlled)
├── feature/
│   ├── auth/
│   ├── posts/
│   ├── events/
│   ├── map/
│   └── profile/
└── MainActivity.kt
```

---

## rchitecture Principles

* **Single Activity (Compose-based)**
* **Feature-driven development**
* **Unidirectional data flow**
* **Clear separation of concerns**

---

## Map System Design

```
MapScreen  
↕  
MapViewModel → MapUiState  
↕  
MapView (reusable)  
↕  
mapengine (logic)  
```

```
EventScreen  
↕  
EventViewModel → EventUiState  
↕  
MapView (reused)  
```

### Rules

* `mapengine/` → logic only (no UI)
* `MapView` → reusable UI component
* Features **must not use mapengine directly**
* Each feature owns its own state

---

## Models

* Defined in `model/`
* Represent app-level data (not strict DB schema)
* Can evolve based on feature needs
* Do NOT duplicate models inside features

---

## Branching Strategy

```
main  → stable
dev   → active development
feature/* → individual features
```

### Rules

* ❌ No direct commits to `main`
* ✔ Work on feature branches
* ✔ Merge into `dev`
* ✔ Pull latest `dev` before starting work

---

## Migration Strategy

* Phase1 code is **read-only reference**
* Features are rewritten one-by-one
* Once rewritten → corresponding phase1 code will be removed

---

## Important Guidelines

* Do not modify `phase1/` unless instructed
* Do not create duplicate models
* Keep UI (Compose) separate from logic
* Follow existing structure — do not invent new patterns

---

## Current Status

* ✅ Compose setup complete
* ✅ Navigation initialized
* ✅ Base architecture ready
* 🚧 Feature development ongoing

---

## Key Principle

```
UI is shared, state is not
```

---

## Team Notes

* Map system is reusable across features
* Models are centrally controlled
* Features are developed independently

---

## Next Steps

* Implement feature screens (posts, map, events)
* Replace phase1 flows gradually
* Integrate backend in later phase

---
