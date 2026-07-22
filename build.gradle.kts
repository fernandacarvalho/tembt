import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.detekt) apply false
}

val libsCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    configure<DetektExtension> {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        // Per-subproject baseline — grandfathers pre-existing violations so CI starts
        // green; burn these down over time. Generate/refresh with `./gradlew detektBaseline`.
        baseline = file("detekt-baseline.xml")
        // KMP source sets — nonexistent dirs are skipped, so this is safe for both
        // the multiplatform `shared` module and the plain-JVM `androidApp` module.
        source.setFrom(
            "src/commonMain/kotlin",
            "src/androidMain/kotlin",
            "src/iosMain/kotlin",
            "src/commonTest/kotlin",
            "src/main/kotlin",
        )
    }

    dependencies {
        add("detektPlugins", libsCatalog.findLibrary("detekt-formatting").get())
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "17"
        reports {
            html.required.set(true)
            xml.required.set(true)
        }
    }
}
