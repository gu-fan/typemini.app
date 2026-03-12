package com.typemini.app

import android.app.Application
import androidx.room.Room
import com.typemini.app.data.db.TypeMiniDatabase
import com.typemini.app.data.repository.DefaultPracticeRepository
import com.typemini.app.data.repository.PracticeRepository

class TypeMiniApplication : Application() {
    val container: AppContainer by lazy {
        AppContainer(this)
    }
}

class AppContainer(application: Application) {
    private val database by lazy {
        Room.databaseBuilder(
            application,
            TypeMiniDatabase::class.java,
            "typemini.db",
        ).fallbackToDestructiveMigration().build()
    }

    val practiceRepository: PracticeRepository by lazy {
        DefaultPracticeRepository(
            resultDao = database.practiceResultDao(),
        )
    }
}
