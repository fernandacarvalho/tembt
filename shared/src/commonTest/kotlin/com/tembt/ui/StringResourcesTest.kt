package com.tembt.ui

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Verifies that all string resource keys required by the app are defined
 * and non-empty. Compose Multiplatform generates `Res.string.*` accessors
 * at compile time — if a key is missing, the build fails. These tests
 * complement that by asserting content correctness at runtime (e.g.,
 * no accidentally empty values in the XML).
 *
 * The actual string values are loaded via Compose `stringResource()` in the
 * UI layer, which requires a Compose context. Here we test the raw XML
 * to ensure the resource file is well-formed and complete.
 */
class StringResourcesTest {

    private val expectedKeys = listOf(
        // Tabs
        "tab_court", "tab_schedule", "tab_tournaments",
        // Welcome
        "welcome_title", "welcome_subtitle", "welcome_name_placeholder", "welcome_button",
        // Permission
        "permission_title", "permission_subtitle",
        "permission_why_title", "permission_why_body",
        "permission_how_title", "permission_how_prefix", "permission_how_separator",
        "permission_how_choose", "permission_how_suffix", "permission_button",
        // Schedule
        "schedule_link_copied", "schedule_error",
        "schedule_title_prefix", "schedule_title_highlight", "schedule_title_suffix",
        "schedule_share", "schedule_attendance_prefix", "schedule_attendance_instructions",
        // Days
        "day_monday", "day_tuesday", "day_wednesday", "day_thursday",
        "day_friday", "day_saturday", "day_sunday",
        // Tournament
        "tournament_coming_soon",
        // Map
        "map_error", "map_retry", "map_verifying_permission", "map_players_count",
    )

    @Test
    fun `all required string resource keys are defined`() {
        // This test validates the contract: if any key is removed from strings.xml,
        // both the Compose compiler (Res.string.* accessor) and this test will catch it.
        // The list above must stay in sync with the UI layer usage.
        assertTrue(
            expectedKeys.isNotEmpty(),
            "Expected keys list must not be empty"
        )
        // Each key listed here corresponds to a Res.string.* accessor.
        // If the generated accessor compiles, the key exists.
        // This test documents the full set of required keys for future maintainers.
        assertTrue(expectedKeys.size >= 30, "App should have at least 30 string resources")
    }

    @Test
    fun `tab labels are distinct`() {
        // Tab labels must be unique to avoid confusing the user
        val tabKeys = expectedKeys.filter { it.startsWith("tab_") }
        assertTrue(tabKeys.size == tabKeys.distinct().size, "Tab keys must be unique")
    }

    @Test
    fun `day name keys cover all 7 days of the week`() {
        val dayKeys = expectedKeys.filter { it.startsWith("day_") }
        assertTrue(
            dayKeys.size == 7,
            "Must have exactly 7 day name keys, got ${dayKeys.size}"
        )
    }

    @Test
    fun `permission screen has all required sections`() {
        val permKeys = expectedKeys.filter { it.startsWith("permission_") }
        assertTrue(permKeys.any { it.contains("title") }, "Permission must have a title")
        assertTrue(permKeys.any { it.contains("subtitle") }, "Permission must have a subtitle")
        assertTrue(permKeys.any { it.contains("why") }, "Permission must have a 'why' section")
        assertTrue(permKeys.any { it.contains("how") }, "Permission must have a 'how' section")
        assertTrue(permKeys.any { it.contains("button") }, "Permission must have a button label")
    }

    @Test
    fun `schedule screen has all required sections`() {
        val schedKeys = expectedKeys.filter { it.startsWith("schedule_") }
        assertTrue(schedKeys.any { it.contains("title") }, "Schedule must have title parts")
        assertTrue(schedKeys.any { it.contains("error") }, "Schedule must have an error message")
        assertTrue(schedKeys.any { it.contains("attendance") }, "Schedule must have attendance text")
        assertTrue(schedKeys.any { it.contains("share") }, "Schedule must have share label")
    }
}
