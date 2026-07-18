package com.vvf.smartmanager.data.repository

import android.os.StatFs
import com.vvf.smartmanager.core.di.IoDispatcher
import com.vvf.smartmanager.data.local.dao.FileEntryDao
import com.vvf.smartmanager.domain.model.FileCategory
import com.vvf.smartmanager.domain.model.StorageSummary
import com.vvf.smartmanager.domain.repository.StorageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepositoryImpl @Inject constructor(
    private val dao: FileEntryDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : StorageRepository {

    override suspend fun getStorageSummary(): Result<StorageSummary> =
        withContext(ioDispatcher) {
            runCatching {
                val stat = StatFs(File("/storage/emulated/0").path)
                val totalBytes = stat.totalBytes
                val freeBytes = stat.availableBytes
                val usedBytes = totalBytes - freeBytes

                val indexedFiles = dao.listAllFiles()
                val byCategory: Map<FileCategory, Long> = indexedFiles
                    .groupBy { FileCategory.fromExtension(it.name.substringAfterLast('.', "")) }
                    .mapValues { (_, files) -> files.sumOf { it.sizeBytes } }

                StorageSummary(
                    totalBytes = totalBytes,
                    usedBytes = usedBytes,
                    freeBytes = freeBytes,
                    byCategory = byCategory
                )
            }
        }
}
