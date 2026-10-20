package com.simkeeper.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.simkeeper.app.data.repo.DataStoreSettingsRepository
import com.simkeeper.app.data.repo.NotificationLog
import com.simkeeper.app.data.repo.RoomSimRepository
import com.simkeeper.app.data.repo.SettingsRepository
import com.simkeeper.app.data.repo.SimRepository
import com.simkeeper.app.data.repo.SimStateSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindSimRepository(impl: RoomSimRepository): SimRepository

    @Binds
    abstract fun bindSimStateSource(impl: RoomSimRepository): SimStateSource

    @Binds
    abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository

    @Binds
    abstract fun bindNotificationLog(impl: DataStoreSettingsRepository): NotificationLog

    companion object {
        private const val SETTINGS_FILE = "settings"

        @Provides
        @Singleton
        fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            PreferenceDataStoreFactory.create { context.preferencesDataStoreFile(SETTINGS_FILE) }
    }
}
