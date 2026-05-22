package com.example.domain

import com.example.data.SyllabusNode

data class SyllabusTreeItem(
    val node: SyllabusNode,
    val children: List<SyllabusTreeItem>
) {
    val progress: Float
        get() {
            if (children.isEmpty()) {
                return if (node.isChecked) 1f else 0f
            }
            return children.map { it.progress }.average().toFloat()
        }
    
    val state: CheckState
        get() {
            if (children.isEmpty()) {
                return if (node.isChecked) CheckState.CHECKED else CheckState.UNCHECKED
            }
            val allChecked = children.all { it.state == CheckState.CHECKED }
            val allUnchecked = children.all { it.state == CheckState.UNCHECKED }
            return when {
                allChecked -> CheckState.CHECKED
                allUnchecked -> CheckState.UNCHECKED
                else -> CheckState.INDETERMINATE
            }
        }
}

enum class CheckState {
    CHECKED, UNCHECKED, INDETERMINATE
}

object TreeBuilder {
    fun buildTree(nodes: List<SyllabusNode>, parentId: String? = null): List<SyllabusTreeItem> {
        return nodes.filter { it.parentId == parentId }
            .sortedBy { it.orderIndex }
            .map { node ->
                SyllabusTreeItem(
                    node = node,
                    children = buildTree(nodes, node.id)
                )
            }
    }
}
