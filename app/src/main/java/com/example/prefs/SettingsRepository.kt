package com.example.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val THEME_KEY = intPreferencesKey("theme_mode") // 0=System, 1=Light, 2=Dark
    private val PALETTE_KEY = intPreferencesKey("color_palette") // 0=Blue, 1=Green, 2=Purple, 3=Orange
    private val OFFLINE_ID_KEY = stringPreferencesKey("offline_id")
    private val FIRST_RUN_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("first_run")

    val themeModeFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[THEME_KEY] ?: 0
    }

    val colorPaletteFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PALETTE_KEY] ?: 0
    }

    val offlineIdFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[OFFLINE_ID_KEY] ?: run {
            val newId = "User_${UUID.randomUUID().toString().take(6).uppercase()}"
            newId
        }
    }
    
    val isFirstRunFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[FIRST_RUN_KEY] ?: true
    }

    suspend fun setFirstRunCompleted() {
        context.dataStore.edit { prefs -> prefs[FIRST_RUN_KEY] = false }
    }

    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { prefs -> prefs[THEME_KEY] = mode }
    }

    suspend fun setColorPalette(palette: Int) {
        context.dataStore.edit { prefs -> prefs[PALETTE_KEY] = palette }
    }

    suspend fun initializeOfflineIdIfNeeded() {
        context.dataStore.edit { prefs ->
            if (prefs[OFFLINE_ID_KEY] == null) {
                prefs[OFFLINE_ID_KEY] = "User_${UUID.randomUUID().toString().take(6).uppercase()}"
            }
        }
    }
}
