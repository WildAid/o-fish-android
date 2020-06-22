package org.wildaid.ofish.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import org.wildaid.ofish.R
import org.wildaid.ofish.data.Repository

const val OFISH_PROVIDER_SUFFIX = ".fileprovider"
const val CHANNEL_ID = "o_fish_channel"

class OFishApplication : Application() {
    val repository: Repository
        get() = ServiceLocator.provideRepository(this)

    private val lifecycleListener: LifecycleListener by lazy { LifecycleListener(applicationContext) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
        setupLifecycleListener()
    }

    private fun createNotificationChannel(context: Context) {
        val name = context.getString(R.string.channel_name)
        val descriptionText = context.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun setupLifecycleListener() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleListener)
    }
}