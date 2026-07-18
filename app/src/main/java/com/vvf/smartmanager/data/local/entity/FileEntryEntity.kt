package com.vvf.smartmanager.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

/**
 * Indexed representation of a file on disk. Populated by the background indexing worker
 * (Phase 12) and updated immediately by file-manager operations for responsiveness.
 */
@Entity(tableName = "file_entries")
data class FileEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModifiedEpochMillis: Long,
    val mimeType: String?,
    val sha256Hash: String? = null,
    val perceptualHash: Long? = null,
    val extractedOcrText: String? = null,
    val isInTrash: Boolean = false,
    val originalPathBeforeTrash: String? = null
)

/**
 * FTS4 shadow table over [FileEntryEntity] — powers Search Engine (Phase 7) and
 * the OCR-text layer of the search pipeline (Phase 8). `rowid` is kept in sync with
 * [FileEntryEntity.id] by Room automatically because this is a content-linked FTS table.
 */
@Fts4(contentEntity = FileEntryEntity::class)
@Entity(tableName = "file_entries_fts")
data class FileEntryFts(
    val name: String,
    val extractedOcrText: String?
)
