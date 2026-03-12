---
name: ui-performance
description: UI composition and performance rules for Jetpack Compose (Android) and SwiftUI (iOS)
user-invocable: false
---

## General Principles

- Build UI as small, isolated, reusable components
- A component redraws only when its own inputs change
- Never pass the entire state object where a subset suffices — derive and pass only the relevant slice
- No business logic inside UI components — delegate to ViewModel
- Each component is responsible for one visual concern

---

## Jetpack Compose — Android

### Recomposition Control
- Pass primitive or stable types as parameters instead of the full state object
  ```kotlin
  // Correct
  @Composable fun UserCard(name: String, avatarUrl: String)
  // Avoid — recomposes on any field change in UserState
  @Composable fun UserCard(user: UserState)
  ```
- Annotate data classes passed to Composables with `@Stable` or `@Immutable`
  - `@Immutable`: all properties are `val` and deeply immutable
  - `@Stable`: mutable but notifies Compose of changes via `State`
- Extract Composables to top-level functions to avoid capturing unnecessary scope

### `remember` and `derivedStateOf`
- `remember { }`: cache expensive objects across recompositions (regex, formatters, painters)
- `rememberSaveable { }`: persist across configuration changes and process death
- `derivedStateOf { }`: when a value is computed from other states — prevents redundant recompositions
  ```kotlin
  val isButtonEnabled by remember { derivedStateOf { email.isNotBlank() && password.length >= 8 } }
  ```
- Do NOT use `remember` for values that should change with new parameters

### Lists
- Always use `key()` in `LazyColumn`/`LazyRow` with a stable, unique identifier
  ```kotlin
  LazyColumn {
      items(users, key = { it.id }) { user -> UserCard(user.name) }
  }
  ```
- Use `PagingData` / `Flow<PagingData<T>>` for large datasets
- Avoid heavy work inside `items {}` block — pre-process in ViewModel

### State Reading Position
- Read `State` as late (deep) as possible in the composition tree to limit recomposition scope
- Avoid reading `State` in layout/measure/draw phases when it can be read at composition time

### Animations
- Use `Modifier.graphicsLayer { }` for visual transforms — does not trigger recomposition
- Use `AnimatedVisibility` and `AnimatedContent` over manual `if` + transition
- Avoid animating values that cause full tree recomposition

### Lambdas
- Do not create lambdas inline in Composable parameters that reference state; `remember` them
  ```kotlin
  // Avoid — new lambda instance every recomposition
  Button(onClick = { viewModel.onAction(Action.Submit) })
  // Prefer
  val onSubmit = remember { { viewModel.onAction(Action.Submit) } }
  Button(onClick = onSubmit)
  ```

### Anti-patterns
- `mutableStateOf` inside a Composable without `remember`
- Reading `viewModel.uiState.value` directly instead of `collectAsStateWithLifecycle()`
- `Column` with hundreds of children — use `LazyColumn`
- Nested `LazyColumn` without fixed height
- Unused modifiers in `Modifier` chains

---

## SwiftUI — iOS

### View Decomposition
- Break large views into small `struct` components; each manages its own local state
- A view should not render more than one logical section
- Prefer composition over large conditional bodies

### Diffing Optimization
- Conform view model types to `Equatable` and wrap with `EquatableView` to skip re-renders
  ```swift
  struct UserCard: View, Equatable {
      let name: String
      let avatarURL: String
  }
  ```
- Use value types (`struct`) for all view state
- Avoid `AnyView` — use `@ViewBuilder` instead
  ```swift
  // Avoid
  func makeView() -> AnyView { AnyView(Text("Hello")) }
  // Prefer
  @ViewBuilder func makeView() -> some View { Text("Hello") }
  ```

### State Placement
- Place `@State` as close to the view that uses it as possible
- `@StateObject` only in the view that creates/owns the object
- Avoid placing heavy `@StateObject` high in the hierarchy when only a subtree needs it
- `@EnvironmentObject` for truly app-wide state only

### Lists
- Use `List` with `Identifiable` items
- Avoid `ForEach` with index when items have stable identities
- Use `LazyVStack`/`LazyHStack` inside `ScrollView` for large dynamic lists
- Pre-process data in ViewModel, not inside list item views

### Async & Images
- Load images with `AsyncImage` or Kingfisher; never decode on main thread
- Use `.task { }` modifier for async data loading — auto-cancelled on disappear

### Rendering
- `drawingGroup()` for complex overlay compositions — single Metal layer
- Avoid `opacity(0)` to hide views when `if` can remove them from the tree
- Use `GeometryReader` sparingly — causes extra layout passes

### Anti-patterns
- `@ObservedObject` for an object the view owns (should be `@StateObject`)
- Calling async functions in `onAppear` without task management — use `.task {}`
- `NavigationView`/`NavigationStack` deep in the hierarchy — keep at root
- Large `body` with deeply nested closures — extract to sub-views
