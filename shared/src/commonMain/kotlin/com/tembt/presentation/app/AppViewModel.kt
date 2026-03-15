package com.tembt.presentation.app

import androidx.lifecycle.ViewModel
import com.tembt.platform.PlayerStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel(playerStorage: PlayerStorage) : ViewModel() {

    private val _showWelcome = MutableStateFlow(!playerStorage.isRegistered())
    val showWelcome: StateFlow<Boolean> = _showWelcome.asStateFlow()

    fun onRegistered() {
        _showWelcome.value = false
    }
}
