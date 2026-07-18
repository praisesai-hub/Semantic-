package com.vvf.smartmanager.domain.model

/**
 * A single file or directory as shown in the File Manager / Search / Storage screens.
 * This is the domain-layer representation — independent of Room entities or java.io.File.
 */
data class FileItem(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModifiedEpochMillis: Long,
    val mimeType: String?,
    val isInTrash: Boolean = false,
    val originalPathBeforeTrash: String? = null
) {
    val extension: String
        get() = name.substringAfterLast('.', missingDelimiterValue = "")

    val category: FileCategory
        get() = FileCategory.fromExtension(extension)
}

enum class FileCategory {
    IMAGE, VIDEO, AUDIO, DOCUMENT, PDF, ARCHIVE, APK, OTHER;

    companion object {
        private val imageExt = setOf("jpg", "jpeg", "png", "webp", "gif", "heic", "bmp")
        private val videoExt = setOf("mp4", "mkv", "webm", "3gp", "avi", "mov")
        private val audioExt = setOf("mp3", "wav", "ogg", "m4a", "flac")
        private val docExt = setOf("doc", "docx", "txt", "xls", "xlsx", "ppt", "pptx")
        private val archiveExt = setOf("zip", "rar", "7z", "tar", "gz")

        fun fromExtension(extension: String): FileCategory {
            val lower = extension.lowercase()
            return when {
                lower in imageExt -> IMAGE
                lower in videoExt -> VIDEO
                lower in audioExt -> AUDIO
                lower == "pdf" -> PDF
                lower in docExt -> DOCUMENT
                lower in archiveExt -> ARCHIVE
                lower == "apk" -> APK
                else -> OTHER
            }
        }
    }
}
