---
name: kmp-code-style
description: Kotlin Multiplatform code style rules for Kotlin and Swift
user-invocable: false
---

## Kotlin Code Style

### Naming
- Classes/interfaces: `PascalCase`
- Functions/variables: `camelCase`
- Constants: `UPPER_SNAKE_CASE` (top-level `val` in companion object or file)
- Files: match the primary class name; one primary class per file
- `expect`/`actual` declarations: same name across platforms
- Boolean properties/functions: prefix with `is`, `has`, `can`, `should`

### Functions
- Prefer expression body (`= ...`) for single-expression functions
- Max ~30 lines per function; extract if longer
- No more than 3–4 parameters; use data class for more
- Default parameters over overloads when possible
- Trailing lambdas outside parentheses

### Classes
- Prefer `data class` for model/state types
- Prefer `sealed class` / `sealed interface` for exhaustive hierarchies (UI state, results)
- No `open` classes unless explicitly designed for inheritance
- Constructor injection only; no `lateinit var` for injected dependencies
- `companion object` only for factory methods or constants — not logic

### Nullability
- Never use `!!` — handle nullability explicitly with `?.`, `?:`, `let`, `requireNotNull`
- Prefer `Result<T>` or sealed class over nullable return for error paths
- Do not expose nullable types in public API when avoidable

### Coroutines
- Never use `GlobalScope`
- Always name coroutines launched with `launch(CoroutineName("..."))` in production code
- Use `supervisorScope` when child failures should not cancel siblings
- Prefer `StateFlow` over `LiveData` in shared code
- Expose only `StateFlow`/`SharedFlow` from ViewModels, never `MutableStateFlow`

### Formatting
- 4-space indentation
- Max line length: 120 characters
- Trailing commas in multi-line parameter lists
- Blank line between class members of different types (properties vs functions)
- No blank lines at start/end of class body

### Anti-patterns
- No `it` for lambdas with more than one line; use named parameter
- No star imports (`import foo.*`)
- No `Unit` return type annotation on functions that return Unit
- No empty `catch` blocks — at minimum log the error
- No commented-out code — delete it

---

## Swift Code Style

### Naming
- Types: `PascalCase`
- Functions/properties/variables: `camelCase`
- Constants: `camelCase` (Swift convention); `static let` in enum namespace
- Files: match the primary type name

### Types
- Prefer `struct` over `class` for value types (models, UI state)
- Use `enum` with associated values over subclassing for variants
- Prefer `protocol` + extension for shared behavior
- Use `final` on classes not designed for subclassing

### Optionals
- Never force-unwrap (`!`) except in tests or well-justified `fatalError` paths
- Prefer `guard let` over `if let` for early exits
- Use `compactMap` / `flatMap` over optional chaining chains

### Closures
- Always use `[weak self]` in escaping closures that reference `self`
- Name closure parameters; avoid `$0` beyond single-argument transforms
- Trailing closure syntax when last parameter is a closure

### Formatting
- 4-space indentation
- Max line length: 120 characters
- Explicit `return` in multi-line computed properties
- Blank line between methods

### Anti-patterns
- No `_ = result` to discard — use `@discardableResult` or handle explicitly
- No `AnyView` in SwiftUI — use `@ViewBuilder` or generics
- No `print()` in production code — use `Logger`
- No commented-out code
