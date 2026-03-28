package com.tembt.ui.permission

import kotlin.test.Test
import kotlin.test.assertTrue

class SettingsInstructionsTest {

    @Test
    fun `settingsAppName is not blank`() {
        assertTrue(settingsAppName.isNotBlank(), "settingsAppName must not be blank")
    }

    @Test
    fun `settingsPath is not blank`() {
        assertTrue(settingsPath.isNotBlank(), "settingsPath must not be blank")
    }

    @Test
    fun `settingsPath contains navigation separator`() {
        assertTrue(
            settingsPath.contains(">"),
            "settingsPath must contain '>' navigation separators, got: $settingsPath"
        )
    }

    @Test
    fun `settingsOption is not blank`() {
        assertTrue(settingsOption.isNotBlank(), "settingsOption must not be blank")
    }
}
