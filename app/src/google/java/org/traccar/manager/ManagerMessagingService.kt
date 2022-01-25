/*
 * Copyright 2018 - 2021 Anton Tananaev (anton.tananaev@gmail.com)
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
package org.traccar.manager

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import br.com.softquick.rastreio.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ManagerMessagingService : FirebaseMessagingService() {

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_ONE_SHOT
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, br.com.softquick.rastreio.MainActivity::class.java),
            flags
        )

        val channelId =
            remoteMessage.notification?.channelId
                ?: getString(R.string.manager_notification_channel_id)
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_notify)
            .setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setColor(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) getColor(R.color.accent) else Color.BLUE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(
            remoteMessage.hashCode(),
            builder.build()
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("onNewToken", token)
        (application as br.com.softquick.rastreio.GoogleMainApplication).broadcastToken(token)
    }
}
