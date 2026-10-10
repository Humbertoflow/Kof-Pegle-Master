package com.example.peggle.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LevelConfigEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PeggleDatabase : RoomDatabase() {

    abstract fun levelDao(): LevelDao

    companion object {
        @Volatile
        private var INSTANCE: PeggleDatabase? = null

        fun getInstance(context: Context): PeggleDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PeggleDatabase::class.java,
                    "peggle_levels.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
