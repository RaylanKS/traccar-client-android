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
package org.traccar.client

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleObserver
import androidx.preference.*
import br.com.softquick.rastreio.MainMenuHandler
import br.com.softquick.rastreio.R

class MainFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inputDialog = DeviceKeyInputDialog()
        inputDialog.initialize(this)
        inputDialog.isCancelable = false
        initPreferences()
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setHasOptionsMenu(true)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<Preference>(KEY_DEVICE)?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                newValue != null && newValue != ""
            }
        findPreference<Preference>(KEY_INTERVAL)?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                try {
                    newValue != null && (newValue as String).toInt() > 0
                } catch (e: NumberFormatException) {
                    Log.w(TAG, e)
                    false
                }
            }
        val numberValidationListener = Preference.OnPreferenceChangeListener { _, newValue ->
            try {
                newValue != null && (newValue as String).toInt() >= 0
            } catch (e: NumberFormatException) {
                Log.w(TAG, e)
                false
            }
        }
        findPreference<Preference>(KEY_DISTANCE)?.onPreferenceChangeListener =
            numberValidationListener
        findPreference<Preference>(KEY_ANGLE)?.onPreferenceChangeListener = numberValidationListener

        alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val originalIntent = Intent(activity, AutostartReceiver::class.java)
        originalIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }
        alarmIntent = PendingIntent.getBroadcast(activity, 0, originalIntent, flags)

        if (sharedPreferences.getBoolean(KEY_STATUS, false)) {
            startTrackingService(checkPermission = true, initialPermission = false)
        }

        PreferenceManager.setDefaultValues(activity, R.xml.preferences, false)
    }

    class NumericEditTextPreferenceDialogFragment : EditTextPreferenceDialogFragmentCompat() {

        override fun onBindDialogView(view: View) {
            val editText = view.findViewById<EditText>(android.R.id.edit)
            editText.inputType = InputType.TYPE_CLASS_NUMBER
            super.onBindDialogView(view)
        }

        companion object {
            fun newInstance(key: String?): NumericEditTextPreferenceDialogFragment {
                val fragment = NumericEditTextPreferenceDialogFragment()
                val bundle = Bundle()
                bundle.putString(ARG_KEY, key)
                fragment.arguments = bundle
                return fragment
            }
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (sharedPreferences.getString(
                KEY_DEVICE,"")!!.replace(" ","") != "") {
            if (listOf(KEY_INTERVAL, KEY_DISTANCE, KEY_ANGLE).contains(preference.key)) {
                val f: EditTextPreferenceDialogFragmentCompat =
                    NumericEditTextPreferenceDialogFragment.newInstance(preference.key)
                f.setTargetFragment(this, 0)
                f.show(requireFragmentManager(), "androidx.preference.PreferenceFragment.DIALOG")
            } else {
                super.onDisplayPreferenceDialog(preference)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        if (inputDialog.showingBefore) {
            inputDialog.show(requireFragmentManager(), "key_dialog")
        }
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        inputDialog.showingBefore = inputDialog.isShowing()
        if (inputDialog.isShowing())
            inputDialog.dismiss() // To not call onCreateDialog when mainFragment is not initialized (mainFragment == null)
    }

    private fun setPreferencesEnabled(enabled: Boolean) {
        findPreference<Preference>(KEY_DEVICE)?.isEnabled = enabled
        findPreference<Preference>(KEY_INTERVAL)?.isEnabled = enabled
        findPreference<Preference>(KEY_DISTANCE)?.isEnabled = enabled
        findPreference<Preference>(KEY_ANGLE)?.isEnabled = enabled
        findPreference<Preference>(KEY_ACCURACY)?.isEnabled = enabled
        findPreference<Preference>(KEY_BUFFER)?.isEnabled = enabled
        findPreference<Preference>(KEY_WAKELOCK)?.isEnabled = enabled
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == KEY_STATUS) {
            if (sharedPreferences.getString(
                    KEY_DEVICE,"")!!.replace(" ","") != "") {
                if (sharedPreferences.getBoolean(KEY_STATUS, false)) {
                    startTrackingService(checkPermission = true, initialPermission = false)
                } else {
                    stopTrackingService()
                }
            }
        } else if (key == KEY_DEVICE) {
            findPreference<Preference>(KEY_DEVICE)?.summary =
                sharedPreferences.getString(KEY_DEVICE, null)
        }
    }

    private fun initPreferences() {
        if (!sharedPreferences.contains(KEY_DEVICE) || sharedPreferences.getString(
                KEY_DEVICE,
                ""
            ) == ""
        ) {
            if (!inputDialog.isShowing())
                inputDialog.show(parentFragmentManager, "key_dialog")
        } else {
            if (inputDialog.isShowing())
                inputDialog.dismiss()
        }
        findPreference<Preference>(KEY_DEVICE)?.summary =
            sharedPreferences.getString(KEY_DEVICE, null)
    }

    fun returnToMainMenu() {
        MainMenuHandler.openScreen(R.id.menu_home_screen, activity!!)
        activity!!.finish()
    }

    fun onDeviceKeyInputDialogAccepted(input: String) {
        if (input.replace(" ", "") != "") {
            sharedPreferences.edit().putString(KEY_DEVICE, input).apply()
            initPreferences()
            if (inputDialog.isShowing())
                inputDialog.dismiss()
        } else {
            Toast.makeText(
                activity,
                getString(R.string.invalid_key),
                Toast.LENGTH_LONG).show()
        }
    }

    /*
    private fun showBackgroundLocationDialog(context: Context, onSuccess: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        val option = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.packageManager.backgroundPermissionOptionLabel
        } else {
            context.getString(R.string.request_background_option)
        }
        builder.setMessage(context.getString(R.string.request_background, option))
        builder.setPositiveButton(android.R.string.ok) { _, _ -> onSuccess() }
        builder.setNeutralButton(R.string.tutorial) { _, _ -> MainMenuHandler.openURL(HELP_URL, context)}
        builder.setCancelable(false)
        builder.show()
    }
     */

    private fun startTrackingService(checkPermission: Boolean, initialPermission: Boolean) {
        if (sharedPreferences.getString(
                KEY_DEVICE,"")!!.replace(" ","") != "") {
            var permission = initialPermission
            if (checkPermission) {
                val requiredPermissions: MutableSet<String> = HashSet()
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                permission = requiredPermissions.isEmpty()
                if (!permission) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(
                            requiredPermissions.toTypedArray(),
                            PERMISSIONS_REQUEST_LOCATION
                        )
                    }
                    return
                }
            }
            if (permission) {
                setPreferencesEnabled(false)
                ContextCompat.startForegroundService(
                    requireContext(),
                    Intent(activity, TrackingService::class.java)
                )
                alarmManager.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    ALARM_MANAGER_INTERVAL.toLong(), ALARM_MANAGER_INTERVAL.toLong(), alarmIntent
                )
            } else {
                sharedPreferences.edit().putBoolean(KEY_STATUS, false).apply()
                val preference = findPreference<TwoStatePreference>(KEY_STATUS)
                preference?.isChecked = false
            }
        }
    }

    private fun stopTrackingService() {
        alarmManager.cancel(alarmIntent)
        requireActivity().stopService(Intent(activity, TrackingService::class.java))
        setPreferencesEnabled(true)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            var granted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false
                    break
                }
            }
            startTrackingService(false, granted)
        }
    }

    companion object {
        private lateinit var inputDialog: DeviceKeyInputDialog
        private lateinit var sharedPreferences: SharedPreferences
        private lateinit var alarmManager: AlarmManager
        private lateinit var alarmIntent: PendingIntent
        private val TAG = MainFragment::class.java.simpleName
        private var requestingPermissions: Boolean = false
        private const val ALARM_MANAGER_INTERVAL = 15000
        private const val HELP_URL = "https://rastreio.softquick.com.br/location_help.php"
        const val KEY_DEVICE = "id"
        const val KEY_INTERVAL = "interval"
        const val KEY_DISTANCE = "distance"
        const val KEY_ANGLE = "angle"
        const val KEY_ACCURACY = "accuracy"
        const val KEY_STATUS = "status"
        const val KEY_BUFFER = "buffer"
        const val KEY_WAKELOCK = "wakelock"
        private const val PERMISSIONS_REQUEST_LOCATION = 2
        private const val PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 3
    }

}
