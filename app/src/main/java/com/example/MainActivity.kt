package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.prefs.SettingsViewModel
import com.example.ui.MainScreen
import com.example.ui.syllabuses.SyllabusViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
            val colorPalette by settingsViewModel.colorPalette.collectAsStateWithLifecycle()

            MyApplicationTheme(themeMode = themeMode, colorPalette = colorPalette) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val syllabusViewModel: SyllabusViewModel = viewModel()
                    MainScreen(syllabusViewModel, settingsViewModel)
                }
            }
        }
    }
}
