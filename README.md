# tembt

Mobile App available for iOS and Android, built with Kotlin Multiplatform (KMP).

## Getting started

```bash
# 1. Configure os git hooks (uma vez por clone)
make setup

# 2. SwiftLint — necessário só para trabalhar no código Swift de iosApp/
brew install swiftlint
```

`make setup` aponta o git deste repositório para `.githooks/` (config local). O detekt
(lint de Kotlin) não precisa de instalação — vem via Gradle plugin.

## Lint & testes

```bash
make lint          # detekt (Kotlin) + SwiftLint (Swift)
make lint-kotlin   # ./gradlew detekt
make lint-swift    # swiftlint em iosApp/
./gradlew jvmTest  # roda os testes de shared/commonTest (sem Android SDK)
```

O padrão de estilo (indent 4, linha 120, sem `!!`, sem star import, trailing commas,
etc.) vale para todo o Kotlin via detekt (`config/detekt/detekt.yml`) e para o Swift
via SwiftLint (`iosApp/.swiftlint.yml`).

## Fluxo de contribuição

1. Crie uma branch a partir de `main` (push direto na `main` é bloqueado).
2. Abra um Pull Request para `main`.
3. O workflow **PR Checks** roda 5 verificações: `test-kmp`, `lint-kotlin`,
   `build-android`, `build-ios`, `lint-swift`.
4. O merge só é liberado quando todas passam (branch protection).

O hook de pre-commit roda SwiftLint nos `.swift` staged e detekt nos `.kt` staged,
bloqueando o commit em caso de violação. Para pular (não recomendado): `git commit --no-verify`.
