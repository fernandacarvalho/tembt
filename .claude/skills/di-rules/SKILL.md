---
name: di-rules
description: Dependency injection rules — inject only what is needed, remove unused dependencies after edits
user-invocable: false
---

## Core Principles

- **Inject only what the class actually uses.** If a dependency is not referenced in the class body, it must not be in the constructor.
- **Constructor injection only.** No field injection, no service locator (`get()` calls inside functions), no `by inject()` outside of the DI graph root.
- **After any code edit, audit the constructor.** If a refactor removes the last usage of a dependency, remove it from the constructor and from the DI module.
- **Depend on interfaces, not implementations.** Inject `FeatureRepository`, not `FeatureRepositoryImpl`.

---

## Koin — Module Rules

### Module Definition
- Each feature has its own Koin module; no god-module with everything
- Naming: `featureModule`, `networkModule`, `databaseModule`
- Register at the narrowest scope possible:
  - `single { }` — app-scoped singleton (repositories, data sources, Ktor client)
  - `viewModel { }` — scoped to ViewModel lifecycle
  - `factory { }` — new instance every time (use cases, mappers)
- Use cases are always `factory { }` — they are stateless and cheap to create

### Scope
- Never use `single { }` for ViewModels — use `viewModel { }` or `factory { }`
- Never use `single { }` for objects that hold UI state
- Platform-specific dependencies (LocationService, PushService) registered in platform-specific modules using `actual` declarations

### Module Composition
```kotlin
// Correct — focused modules composed at app start
val appModule = module { includes(networkModule, databaseModule, authModule, homeModule) }

// Avoid — single module with all dependencies
val appModule = module {
    single { KtorClient() }
    single { Database() }
    viewModel { AuthViewModel(get(), get(), get(), get()) } // too many deps
}
```

---

## Constructor Injection Rules

### Maximum Dependencies
- ViewModel: max 3–4 use cases injected. If more are needed, question whether the ViewModel covers too many features.
- UseCase: max 1–2 repositories or services. If more are needed, consider splitting the use case.
- Repository implementation: max 2 data sources (remote + local).

### After a Code Edit — Mandatory Check
After removing functionality from a class:
1. Scan the constructor parameters
2. For each parameter, verify it is referenced at least once in the class body
3. Remove any parameter with zero usages
4. Remove the corresponding `get()` call from the Koin module
5. If the removed type is no longer used by *any* class, remove its registration from the module entirely

### Signs of Over-injection
- A constructor parameter that is only passed through to another class without being used directly → extract a composed use case or delegate
- A parameter only used in one private method that is never called → dead code, remove both
- Two parameters of the same interface type → likely a design smell; review

---

## Interface vs Implementation

```kotlin
// Correct
class GetUserUseCase(private val repository: UserRepository)

// Wrong — depends on concrete class, breaks DI testability
class GetUserUseCase(private val repository: UserRepositoryImpl)
```

```kotlin
// Correct Koin registration
single<UserRepository> { UserRepositoryImpl(get(), get()) }

// Wrong — exposes concrete type; consumers can't substitute
single { UserRepositoryImpl(get(), get()) }
```

---

## Circular Dependencies

- Circular dependency between two classes = architecture violation; resolve by introducing an interface or restructuring responsibilities
- If Koin throws `DefinitionOverrideException` or circular dependency error at runtime, fix the graph — do not suppress

---

## Platform-Specific Modules

```kotlin
// commonMain — interface
interface PushNotificationService { ... }

// androidMain — actual + Koin registration
val pushModule = module {
    single<PushNotificationService> { FcmPushNotificationService(get()) }
}

// iosMain — actual + Koin registration
val pushModule = module {
    single<PushNotificationService> { ApnsPushNotificationService() }
}
```

---

---

## Testability Contract

Constructor injection and interface-based dependencies are not only an architecture rule — they are what makes unit testing possible. **A class that cannot accept a spy in place of its dependency is not correctly designed.**

### Rules

- Every constructor parameter must be an **interface** type — not a concrete class
- No dependency may be created inside the class body (`= ConcreteImpl()`, `= KtorClient()`, etc.)
- Platform types (`Context`, `UIViewController`, `HttpClient`) must never appear in a constructor — wrap them behind an interface first

### Single-method dependencies — `fun interface`

When a dependency has exactly one method (e.g., a use case injected into a ViewModel), define it as a `fun interface`. This enables the production `SendLocationUseCase` to implement it, while tests can use a lambda or a minimal spy:

```kotlin
// Correct — testable via lambda or SpySendLocation
fun interface SendLocation {
    suspend operator fun invoke(): Result<Unit>
}

// ViewModel injects the interface, not the use case class
class MapViewModel(
    private val sendLocation: SendLocation,
    ...
)

// Koin binds the real implementation
factory<SendLocation> { SendLocationUseCase(get(), get(), get()) }
```

### Testability checklist

- [ ] Every constructor parameter is an interface (protocol), not a concrete class
- [ ] No dependency instantiated inside the class body
- [ ] Single-method dependencies use `fun interface` so lambda spies are possible
- [ ] Koin module binds the interface type (`factory<Interface> { Impl(...) }`)

---

## Unused Dependency Checklist (run after every edit)

- [ ] Every constructor parameter is referenced in the class body
- [ ] No `get()` in a Koin module for a type that has no consumers
- [ ] No `import` statement for a type that is no longer used
- [ ] No module included in `appModule` that registers zero used bindings
- [ ] ViewModel does not inject a repository directly (must go through a UseCase)
