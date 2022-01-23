/*
 * Copyright 2016 - 2021 Anton Tananaev (anton@traccar.org)
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
 */
package org.traccar.manager

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import br.com.softquick.rastreio.MainMenuHandler
import br.com.softquick.rastreio.R

class MainFragment : WebViewFragment() {
    private lateinit var broadcastManager: LocalBroadcastManager
    private lateinit var mainActivity: MainActivity
    fun setMainActivity(mainActivity: MainActivity) {
        this.mainActivity = mainActivity
        broadcastManager = LocalBroadcastManager.getInstance(mainActivity)
    }

    @Suppress("unused")
    inner class AppJavascriptCommunication {
        @JavascriptInterface
        fun postMessage(message: String) {
            Log.i("postMessage", message)
            if (message.startsWith("login")) {
                broadcastManager.sendBroadcast(Intent(EVENT_LOGIN))
            } else if (message.startsWith("server")) {
                val url = message.substring(7)
                br.com.softquick.rastreio.MainActivity.preferences.edit().putString(MainActivity.PREFERENCE_URL, url).apply()
                activity.runOnUiThread { webView.loadUrl(url) }
            }
        }

        @JavascriptInterface
        fun showAboutPage() {
            MainMenuHandler.showLicensesActivity(mainActivity)
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if ((activity.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                // redirect inside webView, not outside, if the url is from the preference url host.
                if (Uri.parse(url).host == Uri.parse(MainActivity.PREFERENCE_URL).host) {
                    view.loadUrl(url)
                    return false
                }
                // External links opens in external browser
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
                return true
            }
        }
        webView.webChromeClient = webChromeClient
        webView.addJavascriptInterface(AppJavascriptCommunication(), "appInterface")
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView.setOnClickListener { mainActivity.hideToolbar() }
        webView.setOnTouchListener { _: View?, _: MotionEvent? ->
            mainActivity.hideToolbar()
            false
        }
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.setSupportZoom(false)
        val url = MainActivity.PREFERENCE_URL
        webView.loadUrl(url)
        webView.requestFocus()
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val token = intent.getStringExtra(KEY_TOKEN)
            if (token != null) Log.i("MainFragment KEY_TOKEN", token) else Log.i("MainFragment KEY_TOKEN", "null")
            val code = "updateNotificationToken && updateNotificationToken('$token')"
            webView.evaluateJavascript(code, null)
        }
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter(EVENT_TOKEN)
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        broadcastManager.unregisterReceiver(broadcastReceiver)
    }

    private var openFileCallback: ValueCallback<Uri?>? = null
    private var openFileCallback2: ValueCallback<Array<Uri>>? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_FILE_CHOOSER) {
            val result = if (resultCode != Activity.RESULT_OK) null else data?.data
            if (openFileCallback != null) {
                openFileCallback!!.onReceiveValue(result)
                openFileCallback = null
            }
            if (openFileCallback2 != null) {
                openFileCallback2!!.onReceiveValue(if (result != null) arrayOf(result) else arrayOf())
                openFileCallback2 = null
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSIONS_LOCATION) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (geolocationCallback != null) {
                geolocationCallback?.invoke(geolocationRequestOrigin, granted, false)
                geolocationRequestOrigin = null
                geolocationCallback = null
            }
        }
    }

    private var geolocationRequestOrigin: String? = null
    private var geolocationCallback: GeolocationPermissions.Callback? = null
    private val webChromeClient = object : WebChromeClient() {
        override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback
        ) {
            geolocationRequestOrigin = null
            geolocationCallback = null
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    AlertDialog.Builder(activity)
                        .setMessage(R.string.permission_location_rationale)
                        .setNeutralButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                            geolocationRequestOrigin = origin
                            geolocationCallback = callback
                            ActivityCompat.requestPermissions(
                                activity,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                REQUEST_PERMISSIONS_LOCATION
                            )
                        }
                        .show()
                } else {
                    geolocationRequestOrigin = origin
                    geolocationCallback = callback
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_PERMISSIONS_LOCATION
                    )
                }
            } else {
                callback.invoke(origin, true, false)
            }
        }

        override fun onShowFileChooser(
            mWebView: WebView,
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            openFileCallback2?.onReceiveValue(null)
            openFileCallback2 = filePathCallback
            val intent = fileChooserParams.createIntent()
            try {
                startActivityForResult(intent, REQUEST_FILE_CHOOSER)
            } catch (e: ActivityNotFoundException) {
                openFileCallback2 = null
                return false
            }
            return true
        }
    }

    companion object {
        const val EVENT_LOGIN = "eventLogin"
        const val EVENT_TOKEN = "eventToken"
        const val KEY_TOKEN = "keyToken"
        private const val REQUEST_PERMISSIONS_LOCATION = 1
        private const val REQUEST_FILE_CHOOSER = 1
    }
}