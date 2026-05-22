package com.example.ui.syllabuses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(viewModel: SyllabusViewModel) {
    val syllabuses by viewModel.allSyllabuses.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Progress Overview") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            )
        }
    ) { padding ->
        if(syllabuses.isEmpty()){
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                Text("No syllabuses to show progress for.")
            }
        }else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(syllabuses, key = { it.id }) { syllabus ->
                    // For exact progress we need to collect tree logic, but we can't collect easily per item in LazyColumn cleanly. 
                    // To keep it simple, we'll display the name and a placeholder or if we use local viewmodel flow.
                    // Let's implement an item that fetches its own progress
                    SyllabusProgressItem(viewModel, syllabus.id, syllabus.title)
                }
            }
        }
    }
}

@Composable
fun SyllabusProgressItem(viewModel: SyllabusViewModel, syllabusId: String, title: String) {
    val tree by viewModel.getSyllabusTreeFlow(syllabusId).collectAsStateWithLifecycle(initialValue = emptyList())
    val progress = if (tree.isEmpty()) 0f else tree.map { it.progress }.average().toFloat()

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.weight(1f).height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
