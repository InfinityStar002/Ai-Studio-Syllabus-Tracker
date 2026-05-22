package com.example.ui.syllabuses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.CheckState
import com.example.domain.SyllabusTreeItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusDetailScreen(
    viewModel: SyllabusViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val syllabus by viewModel.currentSyllabus.collectAsStateWithLifecycle()
    val rawTree by viewModel.currentTree.collectAsStateWithLifecycle()
    val expandedIds by viewModel.expandedNodeIds.collectAsStateWithLifecycle()

    var showAddDialogForNodeId by remember { mutableStateOf<String?>(null) }
    var isAddingRoot by remember { mutableStateOf(false) }

    val flatTree = remember(rawTree, expandedIds) {
        flattenTree(rawTree, expandedIds, 0)
    }

    val totalProgress = remember(rawTree) {
        if (rawTree.isEmpty()) 0f else rawTree.map { it.progress }.average().toFloat()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(syllabus?.title ?: "Syllabus")
                        if (rawTree.isNotEmpty()) {
                            LinearProgressIndicator(
                                progress = { totalProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        syllabus?.id?.let { id ->
                            viewModel.shareSyllabus(context, id)
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { isAddingRoot = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add root item")
            }
        }
    ) { paddingValues ->
        if (rawTree.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No items yet.\nTap + to add one.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp) // extra padding for FAB
            ) {
                items(flatTree, key = { it.item.node.id }) { flatItem ->
                    val isExpanded = expandedIds.contains(flatItem.item.node.id)
                    NodeItemView(
                        flatItem = flatItem,
                        isExpanded = isExpanded,
                        onToggleExpand = { viewModel.toggleNodeExpanded(flatItem.item.node.id) },
                        onCheckChange = { checked ->
                            viewModel.toggleCheckState(flatItem.item.node.id, checked)
                        },
                        onAddChild = { showAddDialogForNodeId = flatItem.item.node.id },
                        onDelete = { viewModel.deleteNode(flatItem.item.node.id) }
                    )
                }
            }
        }

        if (isAddingRoot || showAddDialogForNodeId != null) {
            var title by remember { mutableStateOf("") }
            val parentIdForNewNode = if (isAddingRoot) null else showAddDialogForNodeId
            
            AlertDialog(
                onDismissRequest = {
                    isAddingRoot = false
                    showAddDialogForNodeId = null
                },
                title = { Text(if (isAddingRoot) "Add Root Item" else "Add Sub-item") },
                text = {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                viewModel.addNode(parentIdForNewNode, title.trim())
                                isAddingRoot = false
                                showAddDialogForNodeId = null
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        isAddingRoot = false
                        showAddDialogForNodeId = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

data class FlatTreeItem(
    val item: SyllabusTreeItem,
    val depth: Int
)

fun flattenTree(
    trees: List<SyllabusTreeItem>, 
    expandedIds: Set<String>, 
    depth: Int = 0
): List<FlatTreeItem> {
    val result = mutableListOf<FlatTreeItem>()
    for (tree in trees) {
        result.add(FlatTreeItem(tree, depth))
        if (expandedIds.contains(tree.node.id)) {
            result.addAll(flattenTree(tree.children, expandedIds, depth + 1))
        }
    }
    return result
}

@Composable
fun NodeItemView(
    flatItem: FlatTreeItem,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onCheckChange: (Boolean) -> Unit,
    onAddChild: () -> Unit,
    onDelete: () -> Unit
) {
    val item = flatItem.item
    val hasChildren = item.children.isNotEmpty()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (flatItem.depth * 24).dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            if (hasChildren) {
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle expand"
                    )
                }
            }
        }

        val toggleState = when (item.state) {
            CheckState.CHECKED -> ToggleableState.On
            CheckState.UNCHECKED -> ToggleableState.Off
            CheckState.INDETERMINATE -> ToggleableState.Indeterminate
        }

        TriStateCheckbox(
            state = toggleState,
            onClick = {
                val nextState = if (toggleState == ToggleableState.On) false else true
                onCheckChange(nextState)
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { if (hasChildren) onToggleExpand() else onCheckChange(toggleState != ToggleableState.On) }
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = item.node.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (item.state == CheckState.CHECKED) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
            )
            if (hasChildren) {
                val percent = (item.progress * 100).toInt()
                Text(
                    text = "$percent% Complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        IconButton(onClick = onAddChild) {
            Icon(Icons.Default.Add, contentDescription = "Add child", tint = MaterialTheme.colorScheme.secondary)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
        }
    }
}
