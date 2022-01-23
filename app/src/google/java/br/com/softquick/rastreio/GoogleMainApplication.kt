package br.com.softquick.rastreio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessaging
import org.traccar.manager.MainFragment

@Suppress("unused")
class GoogleMainApplication : MainApplication() {

    override fun onCreate() {
        // manager
        super.onCreate()
        initializeFirebase()
    }

    fun initializeFirebase() {
        val intentFilter = IntentFilter(MainFragment.EVENT_LOGIN)
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MANAGER_CHANNEL,
                getString(R.string.manager_notification_channel),
                NotificationManager.IMPORTANCE_HIGH
            )
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
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