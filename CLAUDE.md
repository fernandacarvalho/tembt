# CLAUDE.md — tembt

Mobile app for iOS and Android built with Kotlin Multiplatform (KMP).

---

## Project Structure

```
tembt/
├── shared/                         # KMP shared module
│   ├── commonMain/                 # Business logic + shared UI (Compose Multiplatform)
│   │   └── kotlin/com/tembt/
│   │       ├── domain/             # Entities, UseCases, Repository interfaces
│   │       ├── data/               # Repository implementations, DTOs, API
│   │       ├── presentation/       # ViewModels, UiState, UiEvent
│   │       └── ui/                 # Shared Compose screens & theme
│   ├── androidMain/                # Android-specific implementations (expect/actual)
│   └── iosMain/                    # iOS-specific implementations + ComposeUIViewController wrappers
├── androidApp/                     # Android entry point + platform-specific screens (map)
└── iosApp/                         # iOS entry point + platform-specific screens (map)
```

**Rule:** Code lives in `commonMain` by default — including UI screens. Move to platform-specific source sets only when:
- A platform API has no KMP equivalent (e.g., MapKit, APNs, CLLocationManager)
- A third-party SDK is platform-only (e.g., MapLibre for Android)
- Performance or UX requires native implementation

### Compose Multiplatform (CMP) — shared UI

The `shared` module uses **Compose Multiplatform** (`org.jetbrains.compose` plugin). All screens except the map are written once in `shared/commonMain/kotlin/com/tembt/ui/` and rendered on both platforms.

**Android** uses the shared Composables directly (they are Jetpack Compose-compatible).

**iOS** uses `ComposeUIViewController` wrappers defined in `shared/src/iosMain/kotlin/com/tembt/ui/ViewControllers.kt`. Each function returns a `UIViewController` that SwiftUI hosts via `ComposeHostingView` (`UIViewControllerRepresentable`).

```
// iosMain — one function per shared screen
fun welcomeViewController(onRegistered: () -> Unit): UIViewController =
    ComposeUIViewController { TembtTheme { WelcomeScreen(onRegistered = onRegistered) } }
```

```swift
// SwiftUI — thin wrapper
ComposeHostingView { ViewControllersKt.welcomeViewController(onRegistered: { ... }) }
```

**Platform-specific screens (map only):**
- `androidApp/ui/map/` — MapLibre + Android permission APIs
- `iosApp/Map/` — MapKit + CoreLocation

**Theme:** `com.tembt.ui.theme.TembtTheme` / `com.tembt.ui.theme.*` — defined once in `shared/commonMain`. `androidApp/ui/theme/Theme.kt` re-exports from there for backward compatibility.

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

For all other screens, implement a **single shared Compose screen** in `shared/commonMain/kotlin/com/tembt/ui/<feature>/` following the steps in [Adding a New Screen](#adding-a-new-screen).

---

## Adding a New Screen

> **STOP — read before creating any file.**
> New screens are **never** created in `androidApp/` or `iosApp/` (except the map).
> They are written once in `shared/commonMain` and automatically run on both platforms.

Every new screen requires changes in **four** places. Follow these steps in order.

### 1. Shared — create the Composable

Create `shared/src/commonMain/kotlin/com/tembt/ui/<feature>/<Feature>Screen.kt`.

```kotlin
package com.tembt.ui.<feature>

@Composable
fun FeatureScreen(viewModel: FeatureViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ...
}
```

Use `org.koin.compose.viewmodel.koinViewModel` (not the androidx version).
Use `androidx.lifecycle.compose.collectAsStateWithLifecycle` (JetBrains KMP version — same package name).

### 2. iosMain — add a ComposeUIViewController wrapper

Add a new function to `shared/src/iosMain/kotlin/com/tembt/ui/ViewControllers.kt`:

```kotlin
fun featureViewController(): UIViewController =
    ComposeUIViewController { TembtTheme { FeatureScreen() } }
```

If the screen needs a callback (e.g., navigation), pass it as a lambda parameter.

### 3. Android — wire into RootScreen

If the screen appears as a tab, update `androidApp/src/main/kotlin/com/tembt/android/ui/RootScreen.kt`:
- Add a `NavigationBarItem` with an icon from `Icons.Default.*` or `Icons.AutoMirrored.Filled.*`
- Add the screen to the `when (selectedTab)` block (import from `com.tembt.ui.<feature>`)

### 4. iOS — wire into ContentView + register in pbxproj

**4a.** Add the tab to `iosApp/iosApp/ContentView.swift`:
```swift
ComposeHostingView { ViewControllersKt.featureViewController() }
    .tabItem { Label("Nome", systemImage: "symbol.name") }
```

**4b.** Register `ComposeHostingView.swift` is already in the project. No new Swift files are needed for shared screens. Only register new Swift files if you are adding iOS-specific helpers (which should be rare).

If you do need to register a new Swift file in `project.pbxproj`, three sections must be updated (Xcode does not pick up files written from outside the IDE):

```
# 1. PBXFileReference
A0010000000000XX /* File.swift */ = {isa = PBXFileReference; lastKnownFileType = sourcecode.swift; path = File.swift; sourceTree = "<group>"; };

# 2. PBXBuildFile
B0010000000000XX /* File.swift in Sources */ = {isa = PBXBuildFile; fileRef = A0010000000000XX; };

# 3. Add to PBXGroup children and PBXSourcesBuildPhase files
```

Use the next sequential hex IDs after the last ones in the file.

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

## Typography

### Condensed display font

Used for screen titles and heavy display text (e.g. "Bora pro **play?**").

| Platform | Font | License | Files |
|---|---|---|---|
| iOS | Helvetica Neue Condensed | Proprietary — **never ship in Android bundle** | `iosMain/composeResources/font/` |
| Android | Barlow Condensed | SIL Open Font License — safe to distribute | `commonMain/composeResources/font/` |

### API — `com.tembt.ui.theme` (expect/actual)

| Function | Returns | Use for |
|---|---|---|
| `condensedBlackFontFamily()` | Single-font family, Black weight | Heaviest display word (e.g. "play?") |
| `condensedBoldFontFamily()` | Single-font family, Bold weight | Medium display text (e.g. "Bora pro ") |
| `condensedFontFamily()` | Family with both weights registered | Avoid — weight resolution unreliable on iOS |

Always use `condensedBlackFontFamily()` / `condensedBoldFontFamily()` separately instead of a shared `condensedFontFamily()`. This guarantees each `Text` loads the correct file without relying on CMP's weight-matching logic, which is unreliable for custom fonts on iOS.

### Rules

- **Never use** `condensedFontFamily()` with `SpanStyle(fontWeight = ...)` — weight resolution from a multi-font family is broken on iOS CMP.
- **Use separate Text composables** with `Modifier.alignByBaseline()` when mixing Bold + Black in the same line.
- **Never add** iOS font files to `commonMain` or `androidMain` — Helvetica Neue is proprietary.
- **Never add** Barlow or other OFL fonts to `iosMain` if the design calls for Helvetica Neue there.

### iOS font files

All 14 variants are extracted from `HelveticaNeue.ttc` and live in `shared/src/iosMain/composeResources/font/` for reference.

**Important:** The two variants actively used by `FontProvider.ios.kt` (`HelveticaNeue-CondensedBold.otf` and `HelveticaNeue-CondensedBlack.otf`) are in `commonMain/composeResources/font/` — CMP does NOT embed `iosMain` composeResources into the Kotlin/Native framework at runtime. Only `commonMain` resources are reliably bundled on iOS.

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
given [precondition] when [action] [expected outcome]
```

**Never use commas in test function names.** Kotlin/Native (iOS target) does not allow commas in backtick function names and will fail to compile. Use spaces only.

Examples:
- `given permission DENIED on init state is PermissionRequired with DENIED`
- `given registration fails storage is not updated`
- `given state is not Ready when checkin called it is ignored`

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
- **Do not create screen files in `androidApp/ui/` or `iosApp/` — all screens go in `shared/commonMain/kotlin/com/tembt/ui/`** (map is the only exception)
- Do not use `org.koin.androidx.compose.koinViewModel` in shared screens — use `org.koin.compose.viewmodel.koinViewModel`
- Do not write a SwiftUI screen or ViewModelHost for a screen that exists in shared — use `ComposeHostingView` instead
