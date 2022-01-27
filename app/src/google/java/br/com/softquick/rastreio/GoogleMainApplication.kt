/*
 * Copyright 2012 - 2021 Anton Tananaev (anton@traccar.org)
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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessaging
import org.traccar.manager.MainFragment

@Suppress("unused")
class GoogleMainApplication : MainApplication() {

    override fun initializeFirebase() {
        super.initializeFirebase()
        val intentFilter = IntentFilter(MainFragment.EVENT_LOGIN)
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val managerChannel = NotificationChannel(
                getString(R.string.manager_notification_channel_id),
                getString(R.string.manager_notification_channel),
                NotificationManager.IMPORTANCE_HIGH
            )
            managerChannel.enableLights(true)
            managerChannel.lightColor = Color.BLUE
            managerChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                managerChannel
            )
        }
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                if (it.isSuccessful) {
                    broadcastToken(it.result)
                }
            }
        }
    }

    fun broadcastToken(token: String?) {
        val intent = Intent(MainFragment.EVENT_TOKEN)
        if (token != null) Log.d("broadcastToken", token)
        intent.putExtra(MainFragment.KEY_TOKEN, token)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}