/*
 * Copyright 2021 - 2022 Raylan Klitzke Schultz
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
 */
package br.com.softquick.rastreio

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import br.com.softquick.rastreio.MainMenuHandler.handleOnCreateOptionsMenu
import br.com.softquick.rastreio.MainMenuHandler.handleOnOptionsItemSelected

class ChooseAppActivity : AppCompatActivity() {
    private lateinit var managerIntent: Intent
    private lateinit var clientIntent: Intent

    // show the layout only if really needed (when not redirecting to another activity on startup)
    private fun initialize() {
        setContentView(R.layout.choose_app_activity)
        val webInterfaceButton = findViewById<Button>(R.id.button_show_web_interface)
        val deviceTrackerButton = findViewById<Button>(R.id.button_device_tracker)
        webInterfaceButton.setOnClickListener { toWebInterface() }
        deviceTrackerButton.setOnClickListener { toDeviceTracker() }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        managerIntent = Intent(this, org.traccar.manager.MainActivity::class.java)
        clientIntent = Intent(this, org.traccar.client.MainActivity::class.java)

        // Get the last user choice of activity
        val appChoice: Int = if (intent.extras != null && intent.extras!!.getBoolean(
                "reset",
                false
            )
        ) -1 else MainActivity.preferences.getInt("app_choice", -1)
        when (appChoice) {
            0 -> toWebInterface()
            1 -> toDeviceTracker()
            else ->                 // onCreate "completion"
                initialize()
        }
    }

    // Open the Manager
    private fun toWebInterface() {
        MainActivity.preferences.edit().putInt("app_choice", 0).apply()
        startActivity(managerIntent)
        finish()
    }

    // Open the Client
    private fun toDeviceTracker() {
        MainActivity.preferences.edit().putInt("app_choice", 1).apply()
        startActivity(clientIntent)
        finish()
    }

    // Menus
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return handleOnCreateOptionsMenu(menu, this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return handleOnOptionsItemSelected(item, this)
    }
}