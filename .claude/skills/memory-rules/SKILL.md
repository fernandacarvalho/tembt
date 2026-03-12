---
name: memory-rules
description: Memory management rules for Kotlin/KMP and Swift/SwiftUI — no retain cycles, no leaks
user-invocable: false
---

## Kotlin / KMP — Memory Rules

### Coroutines & Scopes
- Always cancel coroutines when the owner is destroyed; call `scope.cancel()` in `onCleared()` (ViewModel) or equivalent lifecycle hook
- Never use `GlobalScope` — it lives for the entire process lifetime
- Use `viewModelScope`, `lifecycleScope`, or a custom `CoroutineScope` tied to a lifecycle
- `launch {}` inside a `ViewModel` must use `viewModelScope`
- In `commonMain` ViewModels, expose a `cancel()` / `clear()` function that iOS calls from `deinit`

### StateFlow / SharedFlow
- `StateFlow` and `SharedFlow` collectors must be tied to a lifecycle scope — never collect in `init {}` without cancellation
- On Android: use `collectAsStateWithLifecycle()` in Compose (not `collectAsState()`)
- On iOS: cancel KMP-NativeCoroutines or SKIE watcher in `onDisappear` or `task` modifier lifetime

### Object References
- Do not store `Context`, `Activity`, `Fragment`, or `UIViewController` in `ViewModel`, `UseCase`, `Repository`, or any shared class
- Do not store platform callbacks or listeners in long-lived objects without clearing them
- Use `WeakReference<T>` when a long-lived object must hold a reference to a short-lived one

### Listeners / Callbacks
- Any object that registers a listener (`addObserver`, `setListener`, etc.) must unregister it in the corresponding teardown (onStop/onDestroy/onCleared)
- Prefer `Flow` over callbacks for reactive streams to leverage structured cancurrency automatically

### Collections
- Do not hold growing collections in singletons or application-scoped objects without eviction
- Prefer `LruCache` or `SoftReference`-backed caches for large objects (images, parsed data)

### Anti-patterns
- `object` (singleton) holding references to Activity, Fragment, or View — forbidden
- Static `HashMap` / `MutableList` growing unbounded — forbidden
- `Handler.postDelayed` without corresponding `removeCallbacks` — memory leak risk

---

## Swift / SwiftUI — Memory Rules

### Retain Cycles
- Every escaping closure that captures `self` must use `[weak self]` unless the closure lifetime is guaranteed shorter than `self`
- `@escaping` parameter callbacks stored as properties: use `weak var delegate` or `[weak self]` in the stored closure
- Completion handlers stored in `DispatchQueue.async` or `OperationQueue`: use `[weak self]`
- `NotificationCenter` observers must be removed in `deinit` or use `.store(in: &cancellables)`

### SwiftUI Ownership
- `@StateObject`: **owns** the object — use only in the view that creates it
- `@ObservedObject`: **does not own** — use in child views receiving the object from parent
- Never use `@StateObject` for an object created outside the view (passed in)
- Never use `@ObservedObject` for an object the view should own (it won't be retained)
- `@EnvironmentObject`: safe for shared state injected high in the tree; never store a strong reference to it manually

### KMP ViewModel in SwiftUI
- Use SKIE or KMP-NativeCoroutines to observe shared `StateFlow`
- Cancel observation using `.task {}` modifier (auto-cancelled on disappear) or manually in `onDisappear`
- The iOS wrapper class holding the KMP ViewModel must be `@StateObject` in the owning view
- iOS wrapper class `deinit` must call `viewModel.cancel()` / `viewModel.clear()`

### Combine / async-await
- Store `AnyCancellable` in `Set<AnyCancellable>` as instance property — not local variable (it gets deallocated immediately)
- `async` tasks started with `Task {}` inside a view must be cancelled; use `.task {}` modifier when possible
- `Task.detached` is rarely needed; prefer structured `async` — document reason if used

### Anti-patterns
- `unowned` instead of `weak` when the lifetime is not guaranteed — risk of crash
- Storing a `UIViewController` or `View` instance in a service or ViewModel
- Delegates declared `strong` (default) when they should be `weak`
- Timer without `invalidate()` in `deinit`
