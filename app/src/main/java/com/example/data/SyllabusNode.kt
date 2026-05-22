package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "syllabus_nodes",
    foreignKeys = [
        ForeignKey(
            entity = Syllabus::class,
            parentColumns = ["id"],
            childColumns = ["syllabusId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("syllabusId"),
        Index("parentId")
    ]
)
data class SyllabusNode(
    @PrimaryKey
    val id: String,
    val syllabusId: String,
    val parentId: String?, // null means root node
    val title: String,
    val isChecked: Boolean = false,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
