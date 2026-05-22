package com.example.data

import com.example.domain.SyllabusTreeItem
import com.example.domain.TreeBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SyllabusRepository(private val dao: SyllabusDao) {

    val allSyllabuses: Flow<List<Syllabus>> = dao.getAllSyllabuses()

    fun getSyllabusById(id: String): Flow<Syllabus?> = dao.getSyllabusById(id)

    fun getNodesForSyllabus(syllabusId: String): Flow<List<SyllabusNode>> = dao.getNodesForSyllabus(syllabusId)

    fun getSyllabusTree(syllabusId: String): Flow<List<SyllabusTreeItem>> {
        return dao.getNodesForSyllabus(syllabusId).map { nodes ->
            TreeBuilder.buildTree(nodes, null)
        }
    }

    suspend fun insertSyllabus(syllabus: Syllabus) {
        dao.insertSyllabus(syllabus)
    }
    
    suspend fun updateSyllabus(syllabus: Syllabus) {
        dao.updateSyllabus(syllabus)
    }
    
    suspend fun deleteSyllabus(id: String) {
        dao.deleteSyllabus(id)
    }

    suspend fun insertNode(node: SyllabusNode) {
        dao.insertNode(node)
    }

    suspend fun deleteNode(id: String) {
        dao.deleteNode(id)
    }
    
    suspend fun deleteNodesForSyllabus(syllabusId: String) {
        dao.deleteNodesBySyllabusId(syllabusId)
    }
    
    suspend fun insertNodes(nodes: List<SyllabusNode>) {
        dao.insertNodes(nodes)
    }

    suspend fun toggleNodeCheckState(
        allNodes: List<SyllabusNode>,
        targetNodeId: String,
        newCheckedState: Boolean
    ) {
        val updates = mutableMapOf<String, SyllabusNode>()

        fun updateDescendants(nodeId: String) {
            val node = allNodes.find { it.id == nodeId } ?: return
            updates[node.id] = node.copy(isChecked = newCheckedState)
            val children = allNodes.filter { it.parentId == nodeId }
            for (child in children) {
                updateDescendants(child.id)
            }
        }
        updateDescendants(targetNodeId)

        fun updateAncestors(nodeId: String) {
            val current = updates[nodeId] ?: allNodes.find { it.id == nodeId } ?: return
            val parentId = current.parentId ?: return
            
            val siblings = allNodes.filter { it.parentId == parentId }.map { sibling ->
                updates[sibling.id] ?: sibling
            }
            
            val allChecked = siblings.all { it.isChecked }
            val allUnchecked = siblings.all { !it.isChecked }
            
            val parent = allNodes.find { it.id == parentId } ?: return
            
            if (allChecked) {
                updates[parent.id] = parent.copy(isChecked = true)
                updateAncestors(parent.id)
            } else if (allUnchecked) {
                updates[parent.id] = parent.copy(isChecked = false)
                updateAncestors(parent.id)
            } else {
                if (parent.isChecked) {
                    updates[parent.id] = parent.copy(isChecked = false)
                    updateAncestors(parent.id)
                }
            }
        }
        updateAncestors(targetNodeId)

        dao.updateNodes(updates.values.toList())
    }
}
