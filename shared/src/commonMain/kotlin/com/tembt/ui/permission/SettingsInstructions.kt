package com.tembt.ui.permission

/** Platform-specific settings path the user must follow to enable location. */
// expect/actual because the exact menu labels differ between iOS and Android.
internal expect val settingsAppName: String
internal expect val settingsPath: String
internal expect val settingsOption: String
