package br.com.softquick.rastreio

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.IntentFilter
import android.os.Build
import androidx.multidex.MultiDexApplication
import org.traccar.client.ServiceReceiver
import org.traccar.client.TrackingService

@Suppress("unused")
open class MainApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        System.setProperty("http.keepAliveDuration", (30 * 60 * 1000).toString())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerChannel()
        }

        // client
        val filter = IntentFilter()
        filter.addAction(TrackingService.ACTION_STARTED)
        filter.addAction(TrackingService.ACTION_STOPPED)
        registerReceiver(ServiceReceiver(), filter)

    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun registerChannel() {
        val channel = NotificationChannel(
            CLIENT_CHANNEL, getString(R.string.client_channel_default), NotificationManager.IMPORTANCE_LOW
        )
        channel.enableVibration(false)
        channel.enableLights(false)
        channel.lockscreenVisibility = Notification.VISIBILITY_SECRET
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    companion object {
        const val CLIENT_CHANNEL = "client_channel_default"
        const val MANAGER_CHANNEL = "manager_channel_default"
    }

}