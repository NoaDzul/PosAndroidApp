package com.example.project1.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "pos_database"
            )
                // .fallbackToDestructiveMigration() // Úsalo solo en desarrollo si cambias las tablas
                .build()

            INSTANCE = instance
            instance
        }
    }
}