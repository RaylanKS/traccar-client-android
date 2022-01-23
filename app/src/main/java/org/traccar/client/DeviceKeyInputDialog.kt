package org.traccar.client

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import br.com.softquick.rastreio.R

class DeviceKeyInputDialog : DialogFragment() {
    var mainFragment: MainFragment? = null
    fun initialize(fragment: MainFragment?) {
        mainFragment = fragment
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = context
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.input_dialog, null)
        return if (context != null && mainFragment != null) {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(R.string.device_key_textfield_title)
                .setView(dialogView)
                .setCancelable(false)
                .setNeutralButton(R.string.contact) { _, _ -> br.com.softquick.rastreio.MainMenuHandler.openContactURL(context)}
                .setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int ->
                    val inputText = dialogView.findViewById<EditText>(R.id.device_key_text)
                    mainFragment!!.onDeviceKeyInputDialogAccepted(inputText.text.toString())
                }
            builder.create()
        } else {
            if (context == null) throw NullPointerException("Context is null") else throw NullPointerException(
                "mainFragment is null"
            )
        }
    }

    companion object {
        private const val CONTACT_URL = "https://wa.me/+5527997894471"
    }
}