package com.tembt.domain.model

// defaultValue is used only when no remote config was ever fetched (first launch offline)
enum class FeatureFlag(val key: String, val defaultValue: Boolean) {
    TAB_SCHEDULE("tab_schedule_enabled", false),
    TAB_TOURNAMENTS("tab_tournaments_enabled", false),
}
