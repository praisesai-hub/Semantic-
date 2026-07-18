package com.vvf.smartmanager.data.repository

import android.content.Context
import com.vvf.smartmanager.core.di.IoDispatcher
import com.vvf.smartmanager.core.security.VaultCryptoManager
import com.vvf.smartmanager.core.security.VaultPinManager
import com.vvf.smartmanager.data.local.dao.VaultItemDao
import com.vvf.smartmanager.data.local.entity.VaultItemEntity
import com.vvf.smartmanager.data.mapper.toDomain
import com.vvf.smartmanager.domain.model.FileItem
import com.vvf.smartmanager.domain.model.VaultItem
import com.vvf.smartmanager.domain.repository.VaultRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: VaultItemDao,
    private val pinManager: VaultPinManager,
    private val cryptoManager: VaultCryptoManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : VaultRepository {

    private val _isUnlocked = MutableStateFlow(false)
    override val isUnlocked: Flow<Boolean> = _isUnlocked.asStateFlow()

    private val vaultRoot: File
        get() = File(context.filesDir, "vault").apply { mkdirs() }

    override suspend fun hasPinConfigured(): Boolean =
        withContext(ioDispatcher) { pinManager.hasPinConfigured() }

    override suspend fun setPin(pin: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            require(pin.length in 4..8) { "PIN 4 से 8 अंकों का होना चाहिए" }
            pinManager.setPin(pin)
        }
    }

    override suspend fun unlockWithPin(pin: String): Result<Boolean> = withContext(ioDispatcher) {
        runCatching {
            val valid = pinManager.verifyPin(pin)
            if (valid) _isUnlocked.value = true
            valid
        }
    }

    override fun lock() {
        _isUnlocked.value = false
    }

    override fun unlockWithBiometric() {
        // BiometricPrompt has already authenticated the user at the OS level by the time
        // this is called (see BiometricAuthHelper) — there is no secret to re-verify here,
        // unlike the PIN path which checks a stored hash.
        _isUnlocked.value = true
    }

    override suspend fun addToVault(file: FileItem): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            require(!file.isDirectory) { "फिलहाल सिर्फ फाइलें Vault में जा सकती हैं, folders नहीं" }
            val source = File(file.path)
            val encryptedName = "${UUID.randomUUID()}.vvfenc"
            val destFile = File(vaultRoot, encryptedName)

            cryptoManager.encryptFile(source, destFile)
            source.delete()

            dao.insert(
                VaultItemEntity(
                    originalPath = file.path,
                    encryptedPath = destFile.absolutePath,
                    displayName = file.name,
                    sizeBytes = file.sizeBytes,
                    addedEpochMillis = System.currentTimeMillis(),
                    mimeType = file.mimeType
                )
            )
            Unit
        }
    }

    override suspend fun removeFromVault(item: VaultItem, restoreTo: String): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching {
                val encryptedFile = File(item.encryptedPath)
                val restoredFile = File(restoreTo, item.displayName)
                require(!restoredFile.exists()) { "${restoredFile.name} पहले से मौजूद है" }

                cryptoManager.decryptFile(encryptedFile, restoredFile)
                encryptedFile.delete()

                dao.delete(
                    VaultItemEntity(
                        id = item.id,
                        originalPath = item.originalPath,
                        encryptedPath = item.encryptedPath,
                        displayName = item.displayName,
                        sizeBytes = item.sizeBytes,
                        addedEpochMillis = item.addedEpochMillis,
                        mimeType = item.mimeType
                    )
                )
                Unit
            }
        }

    override fun observeVaultItems(): Flow<List<VaultItem>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }
}
