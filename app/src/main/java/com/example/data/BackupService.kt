package com.example.data

import android.content.Context
import android.net.Uri
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class BackupModel(
    val syllabuses: List<Syllabus>,
    val nodes: List<SyllabusNode>
)

class BackupService(private val context: Context) {
    // using reflection adapter for simplicity if codegen misses anything, but codegen is preferred.
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(BackupModel::class.java)

    suspend fun exportData(uri: Uri, syllabuses: List<Syllabus>, nodes: List<SyllabusNode>) {
        withContext(Dispatchers.IO) {
            val backup = BackupModel(syllabuses, nodes)
            val json = adapter.toJson(backup)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            }
        }
    }

    suspend fun importData(uri: Uri): BackupModel? {
        return withContext(Dispatchers.IO) {
            try {
                val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                } ?: return@withContext null
                adapter.fromJson(json)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun generateJson(syllabuses: List<Syllabus>, nodes: List<SyllabusNode>): String {
        val backup = BackupModel(syllabuses, nodes)
        return adapter.toJson(backup)
    }

    fun parseJson(json: String): BackupModel? {
        return try {
            adapter.fromJson(json)
        } catch (e: Exception) { null }
    }
}
