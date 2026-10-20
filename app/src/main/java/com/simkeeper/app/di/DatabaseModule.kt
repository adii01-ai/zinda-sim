package com.simkeeper.app.di

import android.content.Context
import androidx.room.Room
import com.simkeeper.app.data.db.AppDatabase
import com.simkeeper.app.data.db.RechargeDao
import com.simkeeper.app.data.db.ReminderPrefDao
import com.simkeeper.app.data.db.SimDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME).build()

    @Provides
    fun provideSimDao(db: AppDatabase): SimDao = db.simDao()

    @Provides
    fun provideRechargeDao(db: AppDatabase): RechargeDao = db.rechargeDao()

    @Provides
    fun provideReminderPrefDao(db: AppDatabase): ReminderPrefDao = db.reminderPrefDao()
}
