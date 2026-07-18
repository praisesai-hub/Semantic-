package com.vvf.smartmanager.core.util

import android.webkit.MimeTypeMap
import java.io.File
import java.security.MessageDigest
import kotlin.math.ln
import kotlin.math.pow

object FileSizeFormatter {
    private val units = arrayOf("B", "KB", "MB", "GB", "TB")

    fun format(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val digitGroups = (ln(bytes.toDouble()) / ln(1024.0)).toInt().coerceIn(0, units.size - 1)
        val value = bytes / 1024.0.pow(digitGroups.toDouble())
        return "%.1f %s".format(value, units[digitGroups])
    }
}

object MimeTypeResolver {
    fun resolve(file: File): String? {
        val extension = file.extension.lowercase()
        if (extension.isEmpty()) return null
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
}

/** Level 1 duplicate detection — exact-content match via SHA-256. */
object Sha256Hasher {
    fun hash(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
