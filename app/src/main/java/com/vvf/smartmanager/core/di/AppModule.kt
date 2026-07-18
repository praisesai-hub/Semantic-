package com.vvf.smartmanager.core.di

import com.vvf.smartmanager.data.repository.DuplicateRepositoryImpl
import com.vvf.smartmanager.data.repository.FileRepositoryImpl
import com.vvf.smartmanager.data.repository.OcrRepositoryImpl
import com.vvf.smartmanager.data.repository.SearchRepositoryImpl
import com.vvf.smartmanager.data.repository.StorageRepositoryImpl
import com.vvf.smartmanager.data.repository.VaultRepositoryImpl
import com.vvf.smartmanager.domain.repository.DuplicateRepository
import com.vvf.smartmanager.domain.repository.FileRepository
import com.vvf.smartmanager.domain.repository.OcrRepository
import com.vvf.smartmanager.domain.repository.SearchRepository
import com.vvf.smartmanager.domain.repository.StorageRepository
import com.vvf.smartmanager.domain.repository.VaultRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFileRepository(impl: FileRepositoryImpl): FileRepository

    @Binds
    @Singleton
    abstract fun bindVaultRepository(impl: VaultRepositoryImpl): VaultRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(impl: StorageRepositoryImpl): StorageRepository

    @Binds
    @Singleton
    abstract fun bindDuplicateRepository(impl: DuplicateRepositoryImpl): DuplicateRepository

    @Binds
    @Singleton
    abstract fun bindOcrRepository(impl: OcrRepositoryImpl): OcrRepository
}
