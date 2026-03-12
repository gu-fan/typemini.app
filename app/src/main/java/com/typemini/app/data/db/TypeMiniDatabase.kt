package com.typemini.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PracticeResultEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class TypeMiniDatabase : RoomDatabase() {
    abstract fun practiceResultDao(): PracticeResultDao
}
