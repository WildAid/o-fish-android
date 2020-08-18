package org.wildaid.ofish.app

import android.content.Context
import org.wildaid.ofish.data.*

object ServiceLocator {
    @Volatile
    private var repository: Repository? = null

    fun provideRepository(context: Context): Repository {
        synchronized(this) {
            return repository
                ?: repository
                ?: createRepository(context.applicationContext)
        }
    }

    private fun createRepository(context: Context): Repository {
        repository = RepositoryImpl(
            RealmDataSource(context),
            LocalDataSource(),
            AndroidDataSource(context)
        )

        return repository as Repository
    }
}