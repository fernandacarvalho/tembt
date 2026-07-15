package com.tembt.presentation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tembt.domain.model.AppTab
import com.tembt.domain.usecase.SessionTabConfig
import com.tembt.platform.PlayerStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel(
    playerStorage: PlayerStorage,
    sessionTabConfig: SessionTabConfig,
) : ViewModel() {

    private val _showWelcome = MutableStateFlow(!playerStorage.isRegistered())
    val showWelcome: StateFlow<Boolean> = _showWelcome.asStateFlow()

    // null while resolving — platforms hold the launch screen until this is set
    private val _enabledTabs = MutableStateFlow<List<AppTab>?>(null)
    val enabledTabs: StateFlow<List<AppTab>?> = _enabledTabs.asStateFlow()

    init {
        viewModelScope.launch {
            _enabledTabs.value = try {
                sessionTabConfig.tabs()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                listOf(AppTab.MAP)
            }
        }
    }

    fun onRegistered() {
        _showWelcome.value = false
    }
}
