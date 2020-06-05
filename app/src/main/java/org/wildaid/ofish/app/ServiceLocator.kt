package org.wildaid.ofish.app

import android.content.Context
import io.realm.Realm
import org.wildaid.ofish.data.*

object ServiceLocator {

    @Volatile
    private var repository: Repository? = null

    fun provideRepository(context: Context): Repository {
        synchronized(this) {
            return repository
                ?: repository
                ?: createRepository(
                    context
                )
        }
    }

    private fun createRepository(context: Context): Repository {
        Realm.init(context)
        return RepositoryImpl(
            RealmDataSource(),
            LocalDataSource(),
            AndroidDataSource(context)
        ).also {
            repository = it
        }
    }
}
