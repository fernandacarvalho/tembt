## O que muda

<!-- Descreva o que este PR faz e por quê. -->

## Checklist

- [ ] Os testes passam localmente (`./gradlew jvmTest`)
- [ ] Detekt limpo (`./gradlew detekt`)
- [ ] SwiftLint limpo (`make lint-swift`) — se houver alteração em `.swift`
- [ ] Novas telas foram criadas em `shared/commonMain` (não em `androidApp`/`iosApp`, exceto o mapa)
- [ ] Sem código de plataforma em `commonMain` (usar `expect/actual`)
- [ ] Novos comportamentos cobertos por testes (TDD)
