/*
 * Copyright 2017 - 2021 Anton Tananaev (anton@traccar.org)
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
package org.traccar.client

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import br.com.softquick.rastreio.MainMenuHandler
import br.com.softquick.rastreio.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return MainMenuHandler.handleOnCreateOptionsMenu(menu, this, intArrayOf(R.id.menu_home_screen, R.id.menu_status))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return MainMenuHandler.handleOnOptionsItemSelected(item, this)
    }
}
