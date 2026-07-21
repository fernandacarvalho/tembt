.PHONY: setup lint lint-kotlin lint-swift

# Wire the versioned hooks directory — run once per clone
setup:
	git config core.hooksPath .githooks
	chmod +x .githooks/pre-commit
	@echo "Git hooks configured. detekt runs via Gradle; for Swift install SwiftLint: brew install swiftlint"

# Run every linter against the whole project
lint: lint-kotlin lint-swift

# Kotlin — detekt (KMP standard: shared + androidApp, all Kotlin source sets)
lint-kotlin:
	./gradlew detekt

# Swift — SwiftLint against iosApp
lint-swift:
	cd iosApp && swiftlint lint --strict --baseline swiftlint-baseline.json --config .swiftlint.yml
