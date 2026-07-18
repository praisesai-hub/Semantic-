package com.vvf.smartmanager.core.security

import android.util.Base64
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encrypts/decrypts individual files moved into the Secure Vault.
 *
 * Algorithm: AES-256-GCM. Each file gets a fresh random 12-byte IV, which is written
 * as a 12-byte header in front of the ciphertext so the file is self-contained.
 * The key itself is stored via [PassphraseProvider] (Keystore-wrapped EncryptedSharedPreferences),
 * never hardcoded — Master Specification v2.0, Section 7.
 */
@Singleton
class VaultCryptoManager @Inject constructor(
    private val passphraseProvider: PassphraseProvider
) {

    private fun secretKey(): SecretKeySpec {
        val keyBytes = Base64.decode(passphraseProvider.getOrCreateVaultKeyBase64(), Base64.NO_WRAP)
        return SecretKeySpec(keyBytes, "AES")
    }

    /** Encrypts [sourceFile] into [destFile]. Caller is responsible for deleting [sourceFile] afterwards. */
    fun encryptFile(sourceFile: File, destFile: File) {
        val iv = ByteArray(IV_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))

        FileOutputStream(destFile).use { out ->
            out.write(iv)
            FileInputStream(sourceFile).use { input ->
                val buffer = ByteArray(BUFFER_SIZE)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    val encrypted = cipher.update(buffer, 0, read)
                    if (encrypted != null) out.write(encrypted)
                }
                val final = cipher.doFinal()
                if (final != null) out.write(final)
            }
        }
    }

    /** Decrypts [sourceFile] (previously written by [encryptFile]) into [destFile]. */
    fun decryptFile(sourceFile: File, destFile: File) {
        FileInputStream(sourceFile).use { input ->
            val iv = ByteArray(IV_LENGTH_BYTES)
            val readIv = input.read(iv)
            require(readIv == IV_LENGTH_BYTES) { "Corrupt vault file: missing IV header" }

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))

            FileOutputStream(destFile).use { out ->
                val buffer = ByteArray(BUFFER_SIZE)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    val decrypted = cipher.update(buffer, 0, read)
                    if (decrypted != null) out.write(decrypted)
                }
                val final = cipher.doFinal()
                if (final != null) out.write(final)
            }
        }
    }

    private companion object {
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_LENGTH_BYTES = 12
        const val GCM_TAG_LENGTH_BITS = 128
        const val BUFFER_SIZE = 8192
    }
}
