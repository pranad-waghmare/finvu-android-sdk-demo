package com.example.app.utils

import android.content.Context
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class GeneralUtils(private val context: Context) {
    fun showDialog(txt: String) {
        AlertDialog.Builder(context)
            .setTitle(txt)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    fun showPasswordDialog(title: String, onPasswordEntered: (String) -> Unit) {
        val passwordInput = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "Enter password"
        }

        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(passwordInput)
            .setPositiveButton("OK") { dialog, _ ->
                val password = passwordInput.text.toString()
                onPasswordEntered(password)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

}

fun getDisplayText(acctType: String, fiType: String): String {
    return when (fiType) {
        "TERM_DEPOSIT" -> {
            "TERM DEPOSIT"
        }
        "RECURRING_DEPOSIT" -> {
            "RECURRING DEPOSIT"
        }
        "MUTUAL_FUNDS" -> {
            "MUTUAL FUNDS"
        }
        "INSURANCE_POLICIES" -> {
            "INSURANCE POLICY"
        }
        else -> {
            acctType
        }
    }
}
