package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SyllabusDao {
    @Query("SELECT * FROM syllabuses ORDER BY updatedAt DESC")
    fun getAllSyllabuses(): Flow<List<Syllabus>>

    @Query("SELECT * FROM syllabuses WHERE id = :id LIMIT 1")
    fun getSyllabusById(id: String): Flow<Syllabus?>

    @Query("SELECT * FROM syllabus_nodes WHERE syllabusId = :syllabusId ORDER BY orderIndex ASC")
    fun getNodesForSyllabus(syllabusId: String): Flow<List<SyllabusNode>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyllabus(syllabus: Syllabus)

    @Update
    suspend fun updateSyllabus(syllabus: Syllabus)

    @Query("DELETE FROM syllabuses WHERE id = :id")
    suspend fun deleteSyllabus(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNodes(nodes: List<SyllabusNode>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: SyllabusNode)

    @Update
    suspend fun updateNode(node: SyllabusNode)

    @Update
    suspend fun updateNodes(nodes: List<SyllabusNode>)

    @Query("DELETE FROM syllabus_nodes WHERE id = :id")
    suspend fun deleteNode(id: String)
    
    @Query("DELETE FROM syllabus_nodes WHERE syllabusId = :syllabusId")
    suspend fun deleteNodesBySyllabusId(syllabusId: String)
}
