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

import android.accounts.NetworkErrorException
import android.content.Intent
import android.content.SharedPreferences
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {


    lateinit var termsLayout : LinearLayout
    lateinit var loadingLayout : LinearLayout
    lateinit var loadingProgressBar : ProgressBar
    lateinit var loadingFailLayout: LinearLayout

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.accept_terms_activity)
        preferences = getSharedPreferences("data", MODE_PRIVATE)
        acceptedTermsVersion = preferences.getInt("terms_accepted_version", -1)
        acceptedPrivacyVersion = preferences.getInt("privacy_accepted_version", -1)

        termsLayout = findViewById<LinearLayout>(R.id.accept_terms_layout)
        loadingLayout = findViewById<LinearLayout>(R.id.loading_terms_layout)
        loadingProgressBar = findViewById<ProgressBar>(R.id.loading_terms_progress_bar)
        loadingFailLayout = findViewById<LinearLayout>(R.id.loading_terms_fail_layout)

        val httpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        val formBody = FormBody.Builder()
            .add("terms_accepted_version", acceptedTermsVersion.toString())
            .add("privacy_accepted_version", acceptedPrivacyVersion.toString())
            .build()

        val request = Request.Builder()
            .url(URL_TERMS_PRIVACY_VERSION_CHECKER)
            .post(formBody)
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError()
                Log.e("HttpError","Request failure: ${e.stackTrace}")
            }
            override fun onResponse(call: Call, response: Response) {
                onError()
                Log.e("HttpError","Invalid response from server: $response")
                return;
                if (!response.isSuccessful) {
                    onError()
                    Log.e("HttpError","Invalid response from server: $response")
                }
                val responseContent = response.body?.string() ?: throw NullPointerException("Response body is null")

                val mainJsonObject = JSONObject(responseContent)
                val versionsJsonObject = mainJsonObject.getJSONObject("versions")
                currentTermsVersion = versionsJsonObject.getInt("terms_version")
                currentPrivacyVersion = versionsJsonObject.getInt("privacy_version")
                val appVersion = versionsJsonObject.getInt("app_version")

                if (currentTermsVersion <= acceptedTermsVersion && currentPrivacyVersion <= acceptedPrivacyVersion)
                    next()
                else {
                    loadingLayout.visibility = View.GONE
                    termsLayout.visibility = View.VISIBLE
                    initializeTermsLayoutCode()
                }
            }
        })

    }

    private fun onError() {
        runOnUiThread {
            Toast.makeText(
                applicationContext,
                "Error",
                Toast.LENGTH_LONG
            ).show()
            loadingProgressBar.visibility = View.GONE
            loadingFailLayout.visibility = View.VISIBLE
        }
    }

    private fun initializeTermsLayoutCode() {
        runOnUiThread {

            // Get layout contents
            val acceptTermsCheckbox = findViewById<CheckBox>(R.id.accept_terms_checkbox)
            acceptTermsCheckbox.isChecked =
                false // For safety reasons (if checkbox is checked by accident on the layout xml file)
            // Same for privacy policy checkbox
            val acceptPrivacyCheckbox = findViewById<CheckBox>(R.id.accept_privacy_checkbox)
            acceptPrivacyCheckbox.isChecked = false

            val termosUso = findViewById<Button>(R.id.botaoTermosUso)
            val privacidade = findViewById<Button>(R.id.botaoPoliticaPrivacidade)
            val continuar = findViewById<Button>(R.id.terms_next_button)
            val sair = findViewById<Button>(R.id.terms_exit_button)

            // Set events
            acceptTermsCheckbox.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean ->
                updateNextButtonCheckedStatus(
                    acceptTermsCheckbox.isChecked,
                    acceptPrivacyCheckbox.isChecked,
                    continuar
                )
            }
            acceptPrivacyCheckbox.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean ->
                updateNextButtonCheckedStatus(
                    acceptTermsCheckbox.isChecked,
                    acceptPrivacyCheckbox.isChecked,
                    continuar
                )
            }

            termosUso.setOnClickListener { MainMenuHandler.openURL(URL_TERMS_OF_USE, this) }
            privacidade.setOnClickListener { MainMenuHandler.openURL(URL_PRIVACY_POLITY, this) }
            continuar.setOnClickListener {
                if (updateNextButtonCheckedStatus(
                        acceptTermsCheckbox.isChecked,
                        acceptPrivacyCheckbox.isChecked,
                        continuar
                    )
                ) next()
            }
            sair.setOnClickListener { finish() }
        }
    }

    private fun updateNextButtonCheckedStatus(termsChecked : Boolean, privacyChecked : Boolean, nextButton : Button) : Boolean {
        nextButton.isEnabled = termsChecked and privacyChecked
        return nextButton.isEnabled
    }

    private fun next(): Boolean {
        // Save to the preferences the accepted terms user choice.
        acceptedTermsVersion = currentTermsVersion
        acceptedPrivacyVersion = currentPrivacyVersion

        preferences.edit().putInt("terms_accepted_version", acceptedTermsVersion).apply()
        preferences.edit().putInt("privacy_accepted_version", acceptedPrivacyVersion).apply()

        val intent = Intent(this, ChooseAppActivity::class.java)
        startActivity(intent)
        finish()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return MainMenuHandler.handleOnCreateOptionsMenu(menu, this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return MainMenuHandler.handleOnOptionsItemSelected(item, this)
    }

    companion object {

        lateinit var preferences: SharedPreferences

        private var acceptedTermsVersion = -1
        private var acceptedPrivacyVersion = -1

        private var currentTermsVersion = -1
        private var currentPrivacyVersion = -1

        const val URL_TERMS_OF_USE = "https://rastreio.softquick.com.br/termos_de_uso.html"
        const val URL_PRIVACY_POLITY = "https://rastreio.softquick.com.br/politica_de_privacidade.html"
        const val URL_TERMS_PRIVACY_VERSION_CHECKER = "https://rastreio.softquick.com.br/backend/terms_check.php"

    }
}