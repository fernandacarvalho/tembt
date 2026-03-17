---
name: unit-test-rules
description: Standard rules for writing unit tests — behavior validation, Arrange/Act/Assert, spy pattern, coroutine and Flow setup
user-invocable: false
---

## Core Philosophy

Unit tests verify **behaviors**, not implementations. A test answers:

> "Given this context, when this action happens, what should the outcome be?"

Tests must never break because of an internal refactor that does not change observable behavior. If a test breaks after renaming a private method without changing what the class does, the test was wrong.

---

## Test Structure — Arrange / Act / Assert

Every test follows exactly three sections:

```kotlin
@Test
fun `given [precondition], when [action], [expected outcome]`() = runTest(testDispatcher) {
    // Arrange — configure spies, build the subject under test
    spyRepo.willReturn(Result.success(data))
    val vm = MyViewModel(spyRepo)

    // Act — trigger the behavior being tested
    vm.loadData()
    advanceUntilIdle()

    // Assert — verify the observable outcome
    val state = assertIs<MyUiState.Ready>(vm.uiState.value)
    assertEquals(data, state.data)
}
```

Rules:
- Each section separated by a blank line
- Comments `// Arrange`, `// Act`, `// Assert` are mandatory for any test that is not immediately obvious
- **One Act per test.** If you need to trigger two actions to reach the assertion, split into two tests
- **Assert last.** Never assert in the middle of Act

---

## Test Naming

Use the `given / when / outcome` pattern as the test function name:

```kotlin
// Correct — describes the behavior
fun `given permission DENIED on init, state is PermissionRequired with DENIED`()
fun `given registration fails, storage is not updated`()
fun `given state is not Ready, when checkin called, it is ignored`()
fun `given court succeeds but players fail, state is MapReady with empty players`()

// Avoid — describes implementation, not observable behavior
fun `test checkPermission sets denied state`()
fun `loadData calls repository once`()
fun `testSuccess`()
```

---

## Spy Pattern — No Mocking Libraries

KMP targets (iOS, Android) cannot share JVM reflection-based mocking libraries (Mockito, MockK). All test doubles are **hand-written spies** placed in `commonTest/spy/`.

### What a spy is

A spy is a test double that:
1. Implements the production **interface** (protocol)
2. Has a `willReturn(result)` setter to program return values per test
3. Records calls via `callCount` and `lastXxx` properties for interaction verification

```kotlin
// Spy example
class SpyPlayerRepository : PlayerRepository {

    private var result: Result<Unit> = Result.success(Unit)
    var callCount = 0
    var lastUuid: String? = null
    var lastName: String? = null

    fun willReturn(result: Result<Unit>) { this.result = result }

    override suspend fun registerPlayer(uuid: String, name: String): Result<Unit> {
        callCount++
        lastUuid = uuid
        lastName = name
        return result
    }
}
```

### Spy naming

`Spy[InterfaceName]` — e.g., `SpyPlayerRepository`, `SpyLocationService`, `SpyWindowRepository`.

Existing `FakeXxx` classes follow the same pattern and are equally valid. When creating new test doubles, prefer the `SpyXxx` prefix.

### Swift spies (iOS unit tests)

```swift
class SpyPlayerRepository: PlayerRepository {

    var registerCallCount = 0
    var lastUuid: String?
    var lastName: String?
    var registerResult: Result<Void, Error> = .success(())

    func registerPlayer(uuid: String, name: String) async -> Result<Void, Error> {
        registerCallCount += 1
        lastUuid = uuid
        lastName = name
        return registerResult
    }
}
```

### Spy vs Fake vs Stub

| Double | Behavior | When to use |
|--------|----------|-------------|
| **Spy** | Records calls + programmable results | ViewModels, UseCases — verify interactions AND control behavior |
| **Fake** | Lightweight working implementation | Repositories with many methods where call tracking is secondary |
| **Stub** | Fixed return value, no tracking | Leaf dependencies with no behavior to verify |

Default: use a **Spy**. Use a Fake only when a Spy would require wiring up too many methods.

---

## Testability Requirement for Dependencies

A class is only testable if:

1. All dependencies are injected via **constructor** — never created inside the class body
2. All dependency types are **interfaces** (protocols in Swift) — never concrete classes

```kotlin
// Testable — spy can replace any dependency
class RegisterPlayerUseCase(
    private val repository: PlayerRepository,   // interface ✓
    private val storage: PlayerStorage          // interface ✓
)

// Not testable — concrete class, no injection point for a spy
class RegisterPlayerUseCase {
    private val repository = PlayerRepositoryImpl(httpClient) // ✗
}
```

For single-method dependencies, use a `fun interface` so a lambda can act as a spy in tests:

```kotlin
// In production code
fun interface SendLocation {
    suspend operator fun invoke(): Result<Unit>
}

// In tests — lambda spy
val spySendLocation = SendLocation { Result.success(Unit) }

// Or full spy for interaction verification
class SpySendLocation : SendLocation {
    var callCount = 0
    override suspend fun invoke() = Result.success(Unit).also { callCount++ }
}
```

If a class cannot be tested by the above rules, fix the DI **before** writing the test.

---

## ViewModel Tests — Coroutine Setup

ViewModels use `viewModelScope` backed by `Dispatchers.Main`. The test must replace `Main` with a `StandardTestDispatcher` so coroutines are controlled:

```kotlin
private val testDispatcher = StandardTestDispatcher()

@BeforeTest fun setUp() = Dispatchers.setMain(testDispatcher)
@AfterTest  fun tearDown() = Dispatchers.resetMain()

@Test
fun `example`() = runTest(testDispatcher) {
    // testDispatcher is shared between runTest and Dispatchers.Main
    // so advanceUntilIdle() advances both
}
```

| Method | When to use |
|--------|-------------|
| `runCurrent()` | Advance to first suspension — test intermediate states (e.g., Loading guard) |
| `advanceUntilIdle()` | Run all pending coroutines to completion — test final states |
| `advanceTimeBy(ms)` | Simulate time passage for debounce or retry logic |

**Never use `runBlocking` in ViewModel tests.** It blocks the test thread and prevents coroutine scheduling.

---

## Flow / Event Testing (Turbine)

Use Turbine for `SharedFlow` event assertions. Never use manual `collect { }` with channels.

```kotlin
// Correct
vm.uiEvent.test {
    vm.onOpenSettingsRequested()
    advanceUntilIdle()
    assertEquals(MyUiEvent.OpenSettings, awaitItem())
    cancelAndIgnoreRemainingEvents()
}

// Avoid — manual collect is brittle and misses events emitted before collection starts
val events = mutableListOf<MyUiEvent>()
val job = launch { vm.uiEvent.collect { events.add(it) } }
vm.onOpenSettingsRequested()
job.cancel()
assertEquals(1, events.size)
```

---

## What to Test

| Layer | Test? | Cover |
|-------|-------|-------|
| **UseCase** | Yes | Correct args forwarded to spy; success path saves/updates state; failure does not |
| **ViewModel** | Yes | State transitions (Loading → Ready / Error); event emissions; guard conditions (debounce, wrong state) |
| **Mapper / pure function** | Yes | Input → expected output; null/edge cases |
| **Repository impl** | No (unit) | Integration tests when available — too coupled to network/DB |
| **Platform UI (Compose/SwiftUI)** | No (unit) | Manual, screenshot, or UI automation tests |

---

## What NOT to Test

- Private methods or internal state not reachable via the public API
- Framework behavior (Ktor serialization, SQLDelight, Koin graph)
- Trivial delegation with zero logic (`return repository.get()` with no transformation)
- Multiple distinct behaviors in a single test — always split

---

## Pre-submit Checklist

- [ ] Test name follows `given / when / outcome` pattern
- [ ] Arrange / Act / Assert sections are present and separated
- [ ] Every dependency is a spy (not a real implementation)
- [ ] No mocking library imported (`mockk`, `mockito`, etc.)
- [ ] Each test has exactly one Act
- [ ] `advanceUntilIdle()` is called before asserting async state
- [ ] Turbine used for all `SharedFlow` event assertions
- [ ] Both success and failure paths are covered
- [ ] Guard conditions are covered (empty input, wrong state, in-flight duplicate)
