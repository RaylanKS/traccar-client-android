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

package org.traccar.client

import android.app.Dialog
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import br.com.softquick.rastreio.R

class DeviceKeyInputDialog : DialogFragment() {

    lateinit var mainFragment: MainFragment
    fun initialize(fragment: MainFragment) {
        mainFragment = fragment
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = context
        return if (context != null) {
            val inflater = requireActivity().layoutInflater
            val dialogView = inflater.inflate(R.layout.input_dialog, null)
            val locationWarningTextView =
                dialogView.findViewById<TextView>(R.id.input_dialog_message_location_warning)

            // Format text as html
            locationWarningTextView.text = HtmlCompat.fromHtml(
                getString(R.string.location_alert_user),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            // Follow link in text
            locationWarningTextView.movementMethod = LinkMovementMethod.getInstance()

            val okButton = dialogView.findViewById<Button>(R.id.input_dialog_ok_button)
            val cancelButton = dialogView.findViewById<Button>(R.id.input_dialog_cancel_button)
            val contactButton = dialogView.findViewById<Button>(R.id.input_dialog_contact_button)

            val checkBox = dialogView.findViewById<CheckBox>(R.id.input_dialog_checkbox)

            okButton.isEnabled = false

            val builder = AlertDialog.Builder(context)
            builder.setMessage(R.string.device_key_textfield_title)
                .setView(dialogView)

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                okButton.isEnabled = isChecked
            }

            okButton.setOnClickListener {
                val inputText = dialogView.findViewById<EditText>(R.id.device_key_text)
                mainFragment.onDeviceKeyInputDialogAccepted(inputText.text.toString())
            }
            cancelButton.setOnClickListener {
                mainFragment.returnToMainMenu()
            }
            contactButton.setOnClickListener {
                br.com.softquick.rastreio.MainMenuHandler.openContactURL(context)
            }


            builder.create()
        } else {
            throw NullPointerException(javaClass.name + ": context is null")
        }
    }
}