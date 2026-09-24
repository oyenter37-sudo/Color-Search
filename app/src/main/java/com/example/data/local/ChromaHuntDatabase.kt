package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FoundColorEntity::class], version = 1, exportSchema = false)
abstract class ChromaHuntDatabase : RoomDatabase() {

    abstract fun foundColorDao(): FoundColorDao

    companion object {
        @Volatile
        private var INSTANCE: ChromaHuntDatabase? = null

        fun getDatabase(context: Context): ChromaHuntDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChromaHuntDatabase::class.java,
                    "chroma_hunt_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
