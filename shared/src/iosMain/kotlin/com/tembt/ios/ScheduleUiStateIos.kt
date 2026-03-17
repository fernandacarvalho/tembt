package com.tembt.ios

import com.tembt.presentation.schedule.ScheduleUiState

// Flat, Swift-friendly representation of ScheduleUiState.
// Sealed class hierarchies are awkward in Swift/ObjC interop;
// this single class with nullable fields is straightforward to consume from SwiftUI.
class SlotPlayerIos(val name: String)

class WindowSlotIos(
    val time: String,
    val players: List<SlotPlayerIos>
)

class ScheduleUiStateIos(
    val isLoading: Boolean,
    val date: String?,
    val slots: List<WindowSlotIos>,
    val checkedInSlotTime: String?,
    val isCheckingIn: Boolean,
    val error: String?
) {
    companion object {
        fun from(state: ScheduleUiState): ScheduleUiStateIos = when (state) {
            is ScheduleUiState.Loading -> ScheduleUiStateIos(
                isLoading = true,
                date = null,
                slots = emptyList(),
                checkedInSlotTime = null,
                isCheckingIn = false,
                error = null
            )
            is ScheduleUiState.Ready -> ScheduleUiStateIos(
                isLoading = false,
                date = state.window.date,
                slots = state.window.slots.map { slot ->
                    WindowSlotIos(
                        time = slot.time,
                        players = slot.players.map { SlotPlayerIos(it.name) }
                    )
                },
                checkedInSlotTime = state.checkedInSlotTime,
                isCheckingIn = state.isCheckingIn,
                error = null
            )
            is ScheduleUiState.Error -> ScheduleUiStateIos(
                isLoading = false,
                date = null,
                slots = emptyList(),
                checkedInSlotTime = null,
                isCheckingIn = false,
                error = state.message
            )
        }
    }
}
