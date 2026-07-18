package com.vvf.smartmanager.core.di

import android.content.Context
import androidx.room.Room
import com.vvf.smartmanager.core.security.PassphraseProvider
import com.vvf.smartmanager.data.local.AppDatabase
import com.vvf.smartmanager.data.local.dao.FileEntryDao
import com.vvf.smartmanager.data.local.dao.VaultItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

/**
 * Wires Room to SQLCipher using the actively-maintained `net.zetetic:sqlcipher-android`
 * artifact (NOT the deprecated `android-database-sqlcipher` — that one uses a different
 * package, `net.sqlcipher.database.*`, and is no longer updated as of mid-2025).
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    init {
        // Loads the native SQLCipher library. Must happen before SupportOpenHelperFactory is used.
        System.loadLibrary("sqlcipher")
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        passphraseProvider: PassphraseProvider
    ): AppDatabase {
        val passphraseChars = passphraseProvider.getOrCreateDatabasePassphrase()
        // The stored passphrase is a Base64 string (ASCII-only), so UTF-8 encoding round-trips
        // losslessly — safe to convert directly without SQLCipher's now-removed getBytes() helper.
        val passphraseBytes = String(passphraseChars).toByteArray(Charsets.UTF_8)
        val factory = SupportOpenHelperFactory(passphraseBytes)

        return Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideFileEntryDao(db: AppDatabase): FileEntryDao = db.fileEntryDao()

    @Provides
    fun provideVaultItemDao(db: AppDatabase): VaultItemDao = db.vaultItemDao()
}
