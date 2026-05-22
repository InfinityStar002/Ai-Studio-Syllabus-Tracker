package com.example.ui.syllabuses

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.prefs.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel, syllabusViewModel: SyllabusViewModel) {
    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
    val colorPalette by settingsViewModel.colorPalette.collectAsStateWithLifecycle()
    val offlineId by settingsViewModel.offlineId.collectAsStateWithLifecycle()

    val clipboardManager = LocalClipboardManager.current
    var showImportDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            syllabusViewModel.exportData(uri)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            syllabusViewModel.importData(uri)
        }
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("User Profile", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                ElevatedCard(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Offline ID", style = MaterialTheme.typography.labelMedium)
                            Text(offlineId, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                Text("Appearance", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                
                ListItem(
                    headlineContent = { Text("Theme Mode") },
                    leadingContent = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                    trailingContent = {
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { expanded = true }) {
                                Text(when(themeMode) {
                                    1 -> "Light"
                                    2 -> "Dark"
                                    else -> "System"
                                })
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                DropdownMenuItem(text = { Text("System Default") }, onClick = { settingsViewModel.setThemeMode(0); expanded = false })
                                DropdownMenuItem(text = { Text("Light") }, onClick = { settingsViewModel.setThemeMode(1); expanded = false })
                                DropdownMenuItem(text = { Text("Dark") }, onClick = { settingsViewModel.setThemeMode(2); expanded = false })
                            }
                        }
                    }
                )

                ListItem(
                    headlineContent = { Text("Color Palette") },
                    leadingContent = { Icon(Icons.Default.ColorLens, contentDescription = null) },
                    trailingContent = {
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { expanded = true }) {
                                Text(when(colorPalette) {
                                    1 -> "Green"
                                    2 -> "Purple"
                                    3 -> "Orange"
                                    else -> "Blue"
                                })
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                DropdownMenuItem(text = { Text("Blue") }, onClick = { settingsViewModel.setColorPalette(0); expanded = false })
                                DropdownMenuItem(text = { Text("Green") }, onClick = { settingsViewModel.setColorPalette(1); expanded = false })
                                DropdownMenuItem(text = { Text("Purple") }, onClick = { settingsViewModel.setColorPalette(2); expanded = false })
                                DropdownMenuItem(text = { Text("Orange") }, onClick = { settingsViewModel.setColorPalette(3); expanded = false })
                            }
                        }
                    }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                Text("Data & Backup", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                
                ListItem(
                    headlineContent = { Text("Export to File") },
                    supportingContent = { Text("Save your data to a JSON file") },
                    leadingContent = { Icon(Icons.Default.Backup, contentDescription = null) },
                    modifier = Modifier.clickable { exportLauncher.launch("syllabus_backup_${System.currentTimeMillis()}.json") }
                )
                
                ListItem(
                    headlineContent = { Text("Import from File") },
                    supportingContent = { Text("Restore data from a JSON file") },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.Input, contentDescription = null) },
                    modifier = Modifier.clickable { importLauncher.launch(arrayOf("application/json", "*/*")) }
                )
                
                ListItem(
                    headlineContent = { Text("Import from Clipboard") },
                    supportingContent = { Text("Paste JSON data to import") },
                    leadingContent = { Icon(Icons.Default.Check, contentDescription = null) },
                    modifier = Modifier.clickable { 
                        clipboardManager.getText()?.text?.let { json ->
                            syllabusViewModel.importDataFromJson(json)
                        }
                    }
                )
            }
        }
    }
}
