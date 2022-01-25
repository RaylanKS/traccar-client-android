/*
 * Copyright 2021 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Note that changes are made for this file by Raylan Klitzke Schultz
 */

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

        initializeFirebase()
    }

    open fun initializeFirebase() {
        // Only for Google version
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun registerChannel() {

        val channel = NotificationChannel(
            getString(R.string.client_notification_channel_id), getString(R.string.client_channel_default), NotificationManager.IMPORTANCE_LOW
        )
        channel.enableVibration(false)
        channel.enableLights(false)

        channel.lockscreenVisibility = Notification.VISIBILITY_SECRET
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

}