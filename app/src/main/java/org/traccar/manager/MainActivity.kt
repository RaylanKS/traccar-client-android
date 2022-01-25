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
 *
 * Note that changes are made for this file by Raylan Klitzke Schultz
 */
package org.traccar.manager

import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import br.com.softquick.rastreio.MainMenuHandler
import br.com.softquick.rastreio.R

class MainActivity : AppCompatActivity() {
    private lateinit var mainFragment: MainFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_web)
        mainFragment = MainFragment()
        mainFragment.setMainActivity(this)
        val toolbar = findViewById<Toolbar>(R.id.mainToolbar)
        setSupportActionBar(toolbar)
        showManager()
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
            return true
        }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onBackPressed() {
        if (mainFragment.webView.canGoBack())
            mainFragment.webView.goBack()
        else if (supportActionBar!!.isShowing)
            super.onBackPressed()
        else
            supportActionBar!!.show()
    }

    fun hideToolbar() {
        if (supportActionBar!!.isShowing)
            supportActionBar!!.hide()
    }

    private fun showManager() {
        fragmentManager.beginTransaction().add(R.id.webviewFragmentLocation, mainFragment).commit()
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val fragment = fragmentManager.findFragmentById(R.id.webviewFragmentLocation)
        fragment?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return MainMenuHandler.handleOnCreateOptionsMenu(menu, this, intArrayOf(R.id.menu_home_screen))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return MainMenuHandler.handleOnOptionsItemSelected(item, this)
    }

    companion object {
        const val PREFERENCE_URL = "https://rastreio.softquick.com.br/panel/"
    }
}