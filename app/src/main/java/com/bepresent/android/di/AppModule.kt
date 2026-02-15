package com.bepresent.android.di

import android.content.Context
import androidx.room.Room
import com.bepresent.android.data.db.AppIntentionDao
import com.bepresent.android.data.db.BePresentDatabase
import com.bepresent.android.data.db.PresentSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BePresentDatabase {
        return Room.databaseBuilder(
            context,
            BePresentDatabase::class.java,
            "bepresent.db"
        ).build()
    }

    @Provides
    fun provideAppIntentionDao(database: BePresentDatabase): AppIntentionDao {
        return database.appIntentionDao()
    }

    @Provides
    fun providePresentSessionDao(database: BePresentDatabase): PresentSessionDao {
        return database.presentSessionDao()
    }
}
