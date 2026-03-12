---
name: clean-architecture
description: Clean Architecture and SOLID rules for the KMP project
user-invocable: false
---

## Layer Structure

```
Domain       ← innermost, zero external dependencies
Data         ← implements domain interfaces
Presentation ← ViewModels, UI state, UI events
UI           ← Composables (Android) / SwiftUI Views (iOS)
```

## Dependency Rule

Dependencies point **inward only**. Outer layers depend on inner layers; inner layers never depend on outer layers.

- `Domain` imports nothing from `Data`, `Presentation`, or UI frameworks
- `Data` imports `Domain`; never imports `Presentation` or UI
- `Presentation` imports `Domain`; never imports `Data` directly (only via injected interfaces)
- UI imports `Presentation`; never imports `Domain` or `Data` directly

## Domain Layer Rules

- Contains: `Entity`, `UseCase`, repository interfaces, value objects
- Zero imports from: `android.*`, `UIKit`, `Foundation`, Ktor, SQLDelight, Koin, or any third-party lib
- Pure Kotlin only
- Each `UseCase` has exactly one public `suspend operator fun invoke(...)` or `fun invoke(...): Flow<...>`
- Repository interfaces defined here; implementations belong in `Data`
- No `ViewModel` in domain

## Data Layer Rules

- Contains: repository implementations, remote data sources, local data sources, mappers, DTOs
- May import: Ktor, SQLDelight, kotlinx.serialization, platform `actual` implementations
- DTOs/API models never leak into domain — always map to domain entities at the boundary
- `Mapper` functions are pure (no side effects)

## Presentation Layer Rules

- Contains: `ViewModel`, `UiState` (sealed or data class), `UiEvent` (sealed), `UiAction`/`UiIntent`
- ViewModels live in `commonMain`; use `kotlinx.coroutines` and `StateFlow`
- No business logic in ViewModel — delegate to `UseCase`
- ViewModel receives `UseCase` instances via constructor injection
- Expose: `val uiState: StateFlow<FeatureUiState>` and `fun onAction(action: FeatureUiAction)`
- No direct reference to `Context`, `Activity`, or any platform type

## UI Layer Rules (Platform)

- Contains: Composables (Android) and SwiftUI Views (iOS)
- No business logic — all logic in ViewModel
- Collect `StateFlow` with lifecycle awareness
- Navigate via callbacks/events emitted by ViewModel, not by calling use cases directly

## SOLID Violations to Flag

### S — Single Responsibility
- A class doing more than one thing (e.g., ViewModel that also maps DTOs)
- UseCase with multiple public methods covering unrelated actions
- Repository doing both network and DB without delegating to separate data sources

### O — Open/Closed
- `when` / `if-else` chains on type that must be edited each time a new variant is added (use sealed class + polymorphism)
- Adding new behavior by modifying existing classes instead of extending via interface

### L — Liskov Substitution
- Subtype that throws `NotImplementedException` or weakens behavior of the base contract
- Override that changes preconditions or postconditions

### I — Interface Segregation
- Interface with methods that some implementors leave empty or throw
- Large repository interface where callers only use 1–2 methods — split it

### D — Dependency Inversion
- Instantiating concrete implementations with `= ConcreteClass()` instead of injecting via interface
- ViewModel importing a concrete repository class instead of its interface
- `Data` class imported directly in `Presentation`

## Package Structure (expected)

```
shared/
  commonMain/
    domain/
      entity/
      repository/       ← interfaces only
      usecase/
    data/
      repository/       ← implementations
      remote/
        dto/
        datasource/
      local/
        datasource/
      mapper/
    presentation/
      feature/
        FeatureViewModel.kt
        FeatureUiState.kt
        FeatureUiAction.kt
```
