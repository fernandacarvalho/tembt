package com.tembt.presentation.schedule

sealed class ScheduleUiEvent {
    /** Instructs the UI layer to copy [url] to the system clipboard. */
    data class CopyShareLink(val url: String) : ScheduleUiEvent()
}
