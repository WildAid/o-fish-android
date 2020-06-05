package org.wildaid.ofish.app

import android.app.Application
import org.wildaid.ofish.data.Repository

const val OFISH_PROVIDER_SUFFIX = ".fileprovider"

class OFishApplication : Application() {
    val repository: Repository
        get() = ServiceLocator.provideRepository(this)
}