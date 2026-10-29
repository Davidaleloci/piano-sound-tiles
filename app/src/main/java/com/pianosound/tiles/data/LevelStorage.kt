package com.pianosound.tiles.data

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pianosound.tiles.model.LevelData
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class LevelStorage(private val context: Context) {

    private val gson = Gson()
    private val levelsDir = File(context.filesDir, "custom_levels").apply { mkdirs() }
    private val audioDir = File(context.filesDir, "custom_audio").apply { mkdirs() }

    fun saveLevel(level: LevelData): Boolean {
        return try {
            val file = File(levelsDir, "${level.id}.json")
            file.writeText(gson.toJson(level))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getAllCustomLevels(): List<LevelData> {
        return try {
            val files = levelsDir.listFiles { _, name -> name.endsWith(".json") } ?: emptyArray()
            files.mapNotNull { file ->
                try {
                    val json = file.readText()
                    gson.fromJson(json, LevelData::class.java)
                } catch (e: Exception) {
                    null
                }
            }.sortedByDescending { it.dateCreated }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deleteLevel(levelId: String): Boolean {
        return try {
            val file = File(levelsDir, "$levelId.json")
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun copyAudioToInternal(uri: Uri, fileNameHint: String): String? {
        return try {
            val cleanName = fileNameHint.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val destFile = File(audioDir, "${UUID.randomUUID()}_$cleanName")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
