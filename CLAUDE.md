# CLAUDE.md — tembt

Mobile app for iOS and Android built with Kotlin Multiplatform (KMP).

---

## Project Structure

```
tembt/
├── shared/                         # KMP shared module
│   ├── commonMain/                 # 100% shared code (business logic, domain, data)
│   ├── androidMain/                # Android-specific implementations
│   └── iosMain/                    # iOS-specific implementations
├── androidApp/                     # Android UI layer (Jetpack Compose)
└── iosApp/                         # iOS UI layer (SwiftUI)
```

**Rule:** Code lives in `commonMain` by default. Move to platform-specific source sets only when:
- A platform API has no KMP equivalent (e.g., MapKit, APNs)
- Performance or UX requires native implementation
- Third-party SDK is platform-only

---

## Architecture — Clean Architecture + SOLID

### Layers (shared module)

```
Domain       → Entities, UseCases, Repository interfaces (pure Kotlin, no framework deps)
Data         → Repository implementations, remote/local data sources, mappers
Presentation → ViewModels (shared), UI state, UI events
```

### Rules

- **Dependency rule:** outer layers depend on inner layers, never the reverse
- **Domain layer** has zero Android/iOS/framework imports
- **UseCases** are single-responsibility; one action per class
- **Repository interfaces** defined in domain; implementations in data layer
- **ViewModels** live in `commonMain` using `kotlinx-coroutines` and `StateFlow`
- **Dependency injection:** use Koin (multiplatform-compatible)
- **No business logic in UI layer** (Composables or SwiftUI Views)

### SOLID Checklist

- **S** — Each class has one reason to change; UseCases are single-action
- **O** — Extend via interfaces and composition, not inheritance or modification
- **L** — Subtypes must be substitutable for their base types
- **I** — Split large interfaces; prefer small, focused contracts
- **D** — Depend on abstractions (interfaces) injected via constructor

---

## Shared Code Guidelines (`commonMain`)

- Use `expect/actual` only for platform-specific behavior; document the reason in a comment above `expect`
- Use `kotlinx.coroutines` for async; expose `StateFlow` / `SharedFlow` from ViewModels
- Use `kotlinx.serialization` for JSON
- Use `Ktor` for networking
- Use `SQLDelight` for local persistence
- All `Flow` collectors must be cancelled properly; use `viewModelScope` or platform lifecycle wrappers

---

## Memory Management

### Kotlin / Shared

- Avoid circular references between ViewModels and repositories
- Use `WeakReference` when a long-lived object needs a reference to a short-lived one
- Cancel coroutines in `onCleared()` / lifecycle `onDestroy`
- `StateFlow` and `SharedFlow` must have their collectors tied to lifecycle scope
- Do not store `Context` (Android) or platform objects in shared ViewModels

### Android

- Never store `Activity` or `Fragment` references in ViewModels or singletons
- Use `remember` / `rememberSaveable` correctly in Compose; do not hold lambdas that capture Activity
- Prefer `collectAsStateWithLifecycle()` over `collectAsState()` to avoid collecting in background
- Images: use Coil with `LocalContext`; avoid holding Bitmap references beyond their display lifecycle

### iOS (Swift/SwiftUI)

- Use `[weak self]` in all closures that capture `self` where retain cycles are possible
- Use `@StateObject` for ViewModels owned by a view; `@ObservedObject` for injected ones — never mix
- Cancel Kotlin `StateFlow` collection in `onDisappear` / `task` modifier (use KMP-NativeCoroutines or SKIE)
- Avoid `strong` references between parent and child view controllers/coordinators
- Prefer value types (`struct`) over reference types (`class`) for UI state

---

## UI Performance

### General Rules

- Build UI as small, isolated, reusable components
- A component should only redraw when its own state or props change
- Never pass the entire state object where a subset suffices — derive and pass only the relevant slice
- Avoid inline lambdas/closures that create new instances on every recomposition/re-render

### Android — Jetpack Compose

- Mark stable data classes with `@Stable` or `@Immutable` to enable smart recomposition skipping
- Use `remember { }` to cache expensive computations across recompositions
- Use `derivedStateOf { }` when a value is derived from other state to avoid redundant recompositions
- Use `key()` in `LazyColumn`/`LazyRow` items with a stable unique identifier
- Extract `@Composable` lambdas to top-level functions to prevent unnecessary captures
- Avoid reading `State` inside layout/draw phases when it can be read in composition
- Use `Modifier.graphicsLayer` for animations instead of triggering full recomposition
- Profile with Layout Inspector and Composition Tracing before optimizing

```kotlin
// Prefer
@Composable
fun UserCard(name: String, avatar: String) { ... }

// Avoid — forces UserCard to recompose on any UserState change
@Composable
fun UserCard(user: UserState) { ... }
```

### iOS — SwiftUI

- Decompose views into small `struct` components; each manages its own local state
- Use `EquatableView` or conform models to `Equatable` to allow SwiftUI to skip re-renders
- Avoid placing `@StateObject` high in the hierarchy when only a subtree needs it
- Use `.task { }` modifier for async data loading tied to view lifetime
- Prefer `List` with stable `Identifiable` items over `ForEach` with index
- Avoid `AnyView` — it disables type-based diffing; use `@ViewBuilder` instead
- Use `drawingGroup()` for complex overlay compositions that benefit from Metal rendering

```swift
// Prefer
struct UserCard: View, Equatable {
    let name: String
    let avatarURL: String
    ...
}

// Avoid — entire card re-renders on any UserModel change
struct UserCard: View {
    @ObservedObject var user: UserModel
    ...
}
```

---

## Push Notifications

- **Common interface** defined in `commonMain` via `expect class PushNotificationService`
- `androidMain` actual: wraps Firebase Cloud Messaging (FCM)
- `iosMain` actual: wraps APNs via `UNUserNotificationCenter`
- Token registration and notification handling flow through shared `NotificationRepository`
- Deep link routing triggered by notifications is handled in platform UI layer and passed to shared router/navigator
- Never store device token in memory only; persist via `NotificationRepository` → local DB

---

## Maps

- Maps are platform-specific UI; do not attempt to share map view code
- **Android:** use MapLibre (`org.maplibre.gl:android-sdk`) in `androidApp` — open-source, no API key required
- **iOS:** use `MapKit` / `MKMapView` via `UIViewRepresentable` in `iosApp`
- Shared logic (coordinates, POI models, route calculation requests) lives in `commonMain`
- Define `expect class LocationService` in `commonMain`; platform actuals use GPS APIs
- Location permission requests are handled in the platform UI layer, not in shared code

---

## Platform-Specific Screens

**Default is always shared.** Never create a platform-specific screen without explicit user approval.

When evaluating a screen, if any of the criteria below applies, **stop and ask the user before proceeding**. Present:
1. Which criterion was triggered
2. Why a shared implementation would be problematic
3. What the platform-specific alternative would look like
4. The trade-off (more maintenance, duplicated UI logic)

Only proceed with a platform-specific screen after the user explicitly confirms.

### Criteria that require asking

1. The screen relies heavily on a platform-exclusive SDK (camera, ARKit, MapKit, ARCore, biometrics, NFC)
2. The UX pattern is fundamentally different per platform and a shared abstraction would produce a poor experience on one of them (e.g., iOS bottom sheet vs Android modal, iOS navigation gestures)
3. Performance profiling demonstrates that the shared UI layer cannot meet the required frame rate or responsiveness for that specific screen
4. A required third-party SDK is platform-only and has no KMP wrapper

### Example prompt when asking

> "I noticed this screen uses [reason]. A shared implementation is possible but [trade-off].
> I recommend making this screen platform-specific because [specific justification].
> Should I proceed with separate implementations for Android and iOS, or would you prefer to keep it shared and accept the trade-off?"

For all other screens, implement UI in both `androidApp` and `iosApp` consuming the same shared ViewModel without asking.

---

## Adding a New Screen

Every new screen requires changes in three places. Follow these steps in order.

### 1. Android — create the Composable

Create `androidApp/src/main/kotlin/com/tembt/android/ui/<feature>/<Feature>Screen.kt`.

If the screen needs to appear as a tab, register it in `RootScreen.kt`:
- Add a `NavigationBarItem` with a Material icon from `Icons.Default.*` or `Icons.AutoMirrored.Filled.*`
- Add the screen to the `when (selectedTab)` block

### 2. iOS — create the SwiftUI view

Create `iosApp/iosApp/<Feature>/<Feature>Screen.swift`.

**Then register the file in `iosApp/iosApp.xcodeproj/project.pbxproj`** — Xcode does not pick up new files automatically when they are written from outside the IDE. Three sections must be updated:

```
# 1. PBXFileReference — declares the file
A0010000000000XX /* FeatureScreen.swift */ = {isa = PBXFileReference; lastKnownFileType = sourcecode.swift; path = FeatureScreen.swift; sourceTree = "<group>"; };

# 2. PBXBuildFile — adds it to compilation
B0010000000000XX /* FeatureScreen.swift in Sources */ = {isa = PBXBuildFile; fileRef = A0010000000000XX /* FeatureScreen.swift */; };

# 3. PBXGroup — creates the folder group (path = <Feature>)
D0010000000000XX /* Feature */ = {
    isa = PBXGroup;
    children = (
        A0010000000000XX /* FeatureScreen.swift */,
    );
    path = Feature;
    sourceTree = "<group>";
};
```

Also add the group ref to the `iosApp` parent group's `children` list, and add the build file ref to the `PBXSourcesBuildPhase` files list.

Use the next available hex IDs sequentially after the last ones already in the file:
- File refs: `A001000000000001`, `A001000000000002`, … (currently up to `A001000000000011`)
- Build files: `B001000000000001`, `B001000000000002`, … (currently up to `B00100000000000F`)
- Groups: `D001000000000001`, `D001000000000002`, … (currently up to `D001000000000008`)

If the screen appears as a tab, add it to `ContentView.swift` with an SF Symbols icon:
```swift
FeatureScreen()
    .tabItem { Label("Nome", systemImage: "symbol.name") }
```

---

## Naming Conventions

| Layer | Kotlin | Swift |
|---|---|---|
| ViewModel | `FeatureViewModel` | `FeatureViewModel` (SKIE/KMP-NativeCoroutines) |
| UseCase | `GetFeatureUseCase` | — (called from VM) |
| Repository interface | `FeatureRepository` | — |
| Repository impl | `FeatureRepositoryImpl` | — |
| UI State | `FeatureUiState` | `FeatureUiState` |
| UI Event | `FeatureUiEvent` | `FeatureUiEvent` |

---

## Dependencies (preferred)

| Purpose | Library |
|---|---|
| Networking | Ktor |
| Serialization | kotlinx.serialization |
| Local DB | SQLDelight |
| DI | Koin Multiplatform |
| Async | kotlinx.coroutines |
| KMP ↔ Swift flows | SKIE or KMP-NativeCoroutines |
| Images (Android) | Coil 3 (multiplatform) |
| Images (iOS) | Kingfisher or SDWebImageSwiftUI |
| Maps (Android) | maps-compose |
| Maps (iOS) | MapKit (native) |
| Push (Android) | Firebase Cloud Messaging |
| Push (iOS) | APNs + UNUserNotificationCenter |

---

## Test-Driven Development (TDD)

All new behaviors must be covered by tests. Follow the classical TDD cycle:

```
1. Define the behavior (what should the code do?)
2. Write a failing test that describes that behavior
3. Write the minimum code to make the test pass
4. Run the test — confirm it fails first, then passes
5. Refactor if needed, keeping tests green
```

### Test location

```
shared/src/commonTest/kotlin/com/tembt/
  fake/                        # Fake implementations of interfaces (no mocking libraries)
  domain/usecase/              # UseCase unit tests
  presentation/map/            # MapViewModel tests
  presentation/welcome/        # WelcomeViewModel tests
  presentation/schedule/       # ScheduleViewModel tests
```

### Test setup — ViewModel tests

ViewModels use `viewModelScope` which runs on `Dispatchers.Main`. Tests must:

```kotlin
private val testDispatcher = StandardTestDispatcher()

@BeforeTest fun setUp() = Dispatchers.setMain(testDispatcher)
@AfterTest  fun tearDown() = Dispatchers.resetMain()

@Test
fun `behavior description`() = runTest(testDispatcher) {
    // arrange — configure fakes
    // act     — create VM, call methods
    advanceUntilIdle()
    // assert  — check uiState.value
}
```

Use `runCurrent()` to advance only to the first suspension point (e.g., to test the Loading guard).
Use `advanceUntilIdle()` to run all pending coroutines to completion.

### Fakes over mocks

Use hand-written fake implementations in `commonTest/fake/`. Each fake:
- Implements the production interface
- Has a `willReturn(result)` setter to control behavior per test
- Exposes `callCount` / `lastXxx` fields to verify interactions

Never use a mocking library. Fakes are explicit, portable across platforms, and have no reflection overhead.

### What to test

| Layer | What to test |
|---|---|
| UseCase | Input/output contract: correct args forwarded, success saves to storage, failure does not |
| ViewModel | State transitions per event: Loading → Ready/Error, debounce guards, event emissions |
| Repository | Not tested in unit tests — covered by integration tests when available |
| Platform UI | Not unit-tested — Composables and SwiftUI views are verified manually / via snapshot tests |

### Testing SharedFlow events

Use Turbine for asserting `SharedFlow` emissions:

```kotlin
vm.uiEvent.test {
    vm.onOpenSettingsRequested()
    advanceUntilIdle()
    assertEquals(MapUiEvent.OpenAppSettings, awaitItem())
}
```

### Naming conventions for tests

```
given [precondition], when [action], [expected outcome]
```

Examples:
- `given permission DENIED on init, state is PermissionRequired with DENIED`
- `given registration fails, storage is not updated`
- `given state is not Ready, when checkin called, it is ignored`

---

## What NOT to Do

- Do not use `GlobalScope` — always use structured concurrency
- Do not hold platform references (`Context`, `Activity`, `UIViewController`) in shared code
- Do not use `lateinit var` for injected dependencies when constructor injection is possible
- Do not use `!!` (non-null assertion) — handle nullability explicitly
- Do not skip `@Stable`/`@Immutable` on Compose data classes passed as parameters
- Do not collect flows without cancellation; always tie collection to a lifecycle or scope
- Do not add platform-specific code to `commonMain` — use `expect/actual`
- Do not create god-objects or mega-ViewModels covering multiple unrelated features
