package com.tembt.domain.model

data class ScheduleWindow(
    val date: String,
    val startHour: Int,
    val endHour: Int,
    val slots: List<WindowSlot>
)
