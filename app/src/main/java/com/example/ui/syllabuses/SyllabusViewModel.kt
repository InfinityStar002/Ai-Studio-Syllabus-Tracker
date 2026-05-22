package com.example.ui.syllabuses

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BackupService
import com.example.data.Syllabus
import com.example.data.SyllabusNode
import com.example.data.SyllabusRepository
import com.example.domain.SyllabusTreeItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.prefs.SettingsRepository
import kotlinx.coroutines.flow.first
import java.util.UUID

class SyllabusViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = SyllabusRepository(db.syllabusDao())
    private val backupService = BackupService(application)
    private val settingsRepository = SettingsRepository(application)

    val allSyllabuses: StateFlow<List<Syllabus>> = repository.allSyllabuses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expandedNodeIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedSyllabusId = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            if (settingsRepository.isFirstRunFlow.first()) {
                createExampleSyllabus()
                settingsRepository.setFirstRunCompleted()
            }
        }
    }

    private suspend fun createExampleSyllabus() {
        val syllabusId = UUID.randomUUID().toString()
        repository.insertSyllabus(Syllabus(id = syllabusId, title = "Example Syllabus: Android App Dev"))

        val nodes = mutableListOf<SyllabusNode>()
        
        // Root 1
        val basicsId = UUID.randomUUID().toString()
        nodes.add(SyllabusNode(id = basicsId, syllabusId = syllabusId, parentId = null, title = "Android Basics", orderIndex = 0))
        
        nodes.add(SyllabusNode(id = UUID.randomUUID().toString(), syllabusId = syllabusId, parentId = basicsId, title = "Activities & Intents", orderIndex = 0, isChecked = true))
        nodes.add(SyllabusNode(id = UUID.randomUUID().toString(), syllabusId = syllabusId, parentId = basicsId, title = "Views & Layouts", orderIndex = 1, isChecked = true))
        
        // Root 2
        val composeId = UUID.randomUUID().toString()
        nodes.add(SyllabusNode(id = composeId, syllabusId = syllabusId, parentId = null, title = "Jetpack Compose", orderIndex = 1))
        
        nodes.add(SyllabusNode(id = UUID.randomUUID().toString(), syllabusId = syllabusId, parentId = composeId, title = "State & Modifiers", orderIndex = 0, isChecked = false))
        val layoutId = UUID.randomUUID().toString()
        nodes.add(SyllabusNode(id = layoutId, syllabusId = syllabusId, parentId = composeId, title = "Standard Layouts", orderIndex = 1))
        nodes.add(SyllabusNode(id = UUID.randomUUID().toString(), syllabusId = syllabusId, parentId = layoutId, title = "Row & Column", orderIndex = 0))
        nodes.add(SyllabusNode(id = UUID.randomUUID().toString(), syllabusId = syllabusId, parentId = layoutId, title = "Box & Scaffold", orderIndex = 1))

        repository.insertNodes(nodes)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentTree: StateFlow<List<SyllabusTreeItem>> = selectedSyllabusId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getSyllabusTree(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentSyllabus: StateFlow<Syllabus?> = selectedSyllabusId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getSyllabusById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectSyllabus(id: String?) {
        selectedSyllabusId.value = id
    }

    fun getSyllabusTreeFlow(id: String) = repository.getSyllabusTree(id)

    fun toggleNodeExpanded(nodeId: String) {
        val current = expandedNodeIds.value
        expandedNodeIds.value = if (current.contains(nodeId)) {
            current - nodeId
        } else {
            current + nodeId
        }
    }

    fun expandAll(nodes: List<SyllabusTreeItem>) {
        fun collectIds(items: List<SyllabusTreeItem>): List<String> {
            val ids = mutableListOf<String>()
            for (item in items) {
                ids.add(item.node.id)
                ids.addAll(collectIds(item.children))
            }
            return ids
        }
        expandedNodeIds.value = expandedNodeIds.value + collectIds(nodes).toSet()
    }

    fun collapseAll() {
        expandedNodeIds.value = emptySet()
    }

    fun createSyllabus(title: String) {
        viewModelScope.launch {
            repository.insertSyllabus(
                Syllabus(id = UUID.randomUUID().toString(), title = title)
            )
        }
    }

    fun deleteSyllabus(id: String) {
        viewModelScope.launch {
            repository.deleteSyllabus(id)
            if (selectedSyllabusId.value == id) {
                selectedSyllabusId.value = null
            }
        }
    }

    fun renameSyllabus(id: String, newTitle: String) {
        viewModelScope.launch {
            val syllabus = repository.getSyllabusById(id).first() ?: return@launch
            repository.updateSyllabus(syllabus.copy(title = newTitle))
        }
    }

    fun addNode(parentId: String?, title: String) {
        val syllabusId = selectedSyllabusId.value ?: return
        viewModelScope.launch {
            repository.insertNode(
                SyllabusNode(
                    id = UUID.randomUUID().toString(),
                    syllabusId = syllabusId,
                    parentId = parentId,
                    title = title,
                    orderIndex = System.currentTimeMillis().toInt()
                )
            )
            parentId?.let {
                expandedNodeIds.value += it
            }
        }
    }

    fun deleteNode(id: String) {
        viewModelScope.launch {
            repository.deleteNode(id)
        }
    }

    fun toggleCheckState(nodeId: String, newState: Boolean) {
        val syllabusId = selectedSyllabusId.value ?: return
        viewModelScope.launch {
            val allNodes = repository.getNodesForSyllabus(syllabusId).first()
            repository.toggleNodeCheckState(allNodes, nodeId, newState)
        }
    }
    
    fun exportData(uri: Uri) {
        viewModelScope.launch {
            val syllabuses = repository.allSyllabuses.first()
            val allNodes = mutableListOf<SyllabusNode>()
            for (s in syllabuses) {
                allNodes.addAll(repository.getNodesForSyllabus(s.id).first())
            }
            backupService.exportData(uri, syllabuses, allNodes)
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            val backup = backupService.importData(uri)
            if (backup != null) {
                backup.syllabuses.forEach { repository.insertSyllabus(it) }
                repository.insertNodes(backup.nodes)
            }
        }
    }

    fun importDataFromJson(json: String) {
        viewModelScope.launch {
            val backup = backupService.parseJson(json)
            if (backup != null) {
                backup.syllabuses.forEach { repository.insertSyllabus(it) }
                repository.insertNodes(backup.nodes)
            }
        }
    }

    fun shareSyllabus(context: android.content.Context, syllabusId: String) {
        viewModelScope.launch {
            val syllabus = repository.getSyllabusById(syllabusId).first() ?: return@launch
            val nodes = repository.getNodesForSyllabus(syllabusId).first()
            val json = backupService.generateJson(listOf(syllabus), nodes)
            
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_SUBJECT, "Syllabus: ${syllabus.title}")
                putExtra(android.content.Intent.EXTRA_TEXT, json)
            }
            context.startActivity(android.content.Intent.createChooser(intent, "Share Syllabus JSON"))
        }
    }
}
