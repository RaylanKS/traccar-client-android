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

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.mikepenz.aboutlibraries.LibsBuilder

// This is for easy editing the menu in all activities
object MainMenuHandler {

    private val MENU_HIDE_DEFAULT = intArrayOf(R.id.menu_home_screen, R.id.menu_status)

    // Called in onCreateOptionsMenu
    fun handleOnCreateOptionsMenu(
        menu: Menu,
        activity: Activity,
        menus: IntArray = intArrayOf()
    ): Boolean {
        activity.menuInflater.inflate(R.menu.main_menu, menu)
        R.layout.main
        // Hide menus that we don't want

        for (i in MENU_HIDE_DEFAULT.indices) {
            if (!menus.contains(MENU_HIDE_DEFAULT[i])) {
                menu.findItem(MENU_HIDE_DEFAULT[i]).isVisible = false
            }
        }

        return true
    }

    // Called in onOptionsItemSelected
    fun handleOnOptionsItemSelected(item: MenuItem, activity: Activity): Boolean {
        return openScreen(item.itemId, activity)
    }

    // Open a screen
    fun openScreen(screenMenuId: Int, activity: Activity): Boolean {
        return when (screenMenuId) {
            R.id.menu_open_source_licenses -> {
                showLicensesActivity(activity)
                true
            }
            R.id.menu_termos_uso -> {
                openURL(MainActivity.URL_TERMS_OF_USE, activity)
                true
            }
            R.id.menu_privacidade -> {
                openURL(MainActivity.URL_PRIVACY_POLITY, activity)
                true
            }
            R.id.menu_home_screen -> {
                // Not using switch with getClass().getName() because we might forget to change here if the class name is changed
                when (activity.javaClass) {
                    MainActivity::class.java // To not bypass the Accept Terms screen.
                    -> Toast.makeText(
                        activity,
                        activity.getString(R.string.terms_info),
                        Toast.LENGTH_LONG
                    )
                        .show()
                    ChooseModeActivity::class.java // When already in main screen.
                    -> Toast.makeText(
                        activity,
                        activity.getString(R.string.already_in_main_screen),
                        Toast.LENGTH_LONG
                    ).show()
                    else -> {
                        val intent = Intent(activity, ChooseModeActivity::class.java)
                        // Open ChooseAppActivity without opening the last activity again
                        intent.putExtra("reset", true)
                        activity.startActivity(intent)
                        activity.finish()
                    }
                }
                true
            }
            R.id.menu_status -> {
                activity.startActivity(
                    Intent(
                        activity,
                        org.traccar.client.StatusActivity::class.java
                    )
                )
                true
            }
            R.id.menu_contact -> {
                openContactURL(activity)
                true
            }
            else -> false
        }
    }


    fun openContactURL(context: Context) {
        openURL(CONTACT_URL, context)
    }

    fun openURL(url: String?, context: Context) {
        // Parse url and open in user's browser if possible
        try {
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        } catch (e: Exception) {
            // If not possible (no browser exists on the device), then show a message with the url.
            val builder2 = AlertDialog.Builder(context)
            builder2.setMessage(url)
                .setPositiveButton("ok") { _: DialogInterface?, _: Int -> }
            builder2.setCancelable(true)
            builder2.create().show()
        }
    }

    // Show the license info activity
    fun showLicensesActivity(context: Context?) {
        val libBuilder = LibsBuilder()
            .withActivityTitle(context?.getString(R.string.about_app)!!)
            .withLicenseShown(true)
            .withAboutAppName(context.getString(R.string.app_name))
            .withAboutVersionShown(true)
            .withAboutVersionShownName(true)
            .withAboutVersionShownCode(false)
            .withSearchEnabled(true)
        libBuilder.start(context)
    }

    private const val CONTACT_URL = "https://wa.me/+5527997894471"
}