package org.wildaid.ofish.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ProcessLifecycleOwner
import org.wildaid.ofish.R
import org.wildaid.ofish.util.getString

const val OFISH_PROVIDER_SUFFIX = ".fileprovider"
const val CHANNEL_ID = "o_fish_channel"

class OFishApplication : Application() {
    private val appLifecycleListener: AppLifecycleListener by lazy {
        AppLifecycleListener(
            applicationContext
        )
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
        setupLifecycleListener()
        setupDarkMode(this)
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
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleListener)
    }

    private fun setupDarkMode(context: Context) {
        val sharedPref: SharedPreferences = context.getSharedPreferences(getString(R.string.DARK_MODE_STATE), Context.MODE_PRIVATE)
        val isDarkModeEnabled = sharedPref.getBoolean(getString(R.string.DARK_MODE_ENABLED), false)
        if(isDarkModeEnabled)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}