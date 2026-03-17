package com.tembt.domain.model

data class ScheduleWindow(
    val date: String,
    val slots: List<WindowSlot>
)
