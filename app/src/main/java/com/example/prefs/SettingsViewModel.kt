package com.example.prefs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)

    val themeMode: StateFlow<Int> = repository.themeModeFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        0
    )

    val colorPalette: StateFlow<Int> = repository.colorPaletteFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        0
    )

    val offlineId: StateFlow<String> = repository.offlineIdFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ""
    )

    init {
        viewModelScope.launch {
            repository.initializeOfflineIdIfNeeded()
        }
    }

    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setColorPalette(palette: Int) {
        viewModelScope.launch {
            repository.setColorPalette(palette)
        }
    }
}
