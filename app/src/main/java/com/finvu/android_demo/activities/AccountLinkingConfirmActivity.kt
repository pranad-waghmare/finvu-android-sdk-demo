package com.finvu.android_demo.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.app.utils.GeneralUtils
import com.finvu.android.FinvuManager
import com.finvu.android_demo.databinding.ActivityAccountLinkingConfirmBinding

class AccountLinkingConfirmActivity : AppCompatActivity() {

    private val finvuManager = FinvuManager.shared
    private lateinit var referenceNumber: String
    private var _binding: ActivityAccountLinkingConfirmBinding? = null
    private val v get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAccountLinkingConfirmBinding.inflate(layoutInflater)
        setContentView(v.root)

        v.btnContinue.setOnClickListener {
            if (v.etOtp.text.isNotEmpty())
                accountLinkingConfirmApi()
        }

        if (intent.extras != null) {
            referenceNumber = intent.getStringExtra("referenceNumber").toString()
        }
    }

    private fun accountLinkingConfirmApi() {
        finvuManager.confirmAccountLinking(
            referenceNumber,
            v.etOtp.text.toString(),
        ) { result ->
            runOnUiThread {
                if (result.isSuccess) {
                    val list = result.getOrNull()?.linkedAccounts
                    Log.d("AccountLinkingResponse", list.toString())
                    Toast.makeText(this@AccountLinkingConfirmActivity, "Linked", Toast.LENGTH_SHORT).show()
                    val intent =
                        Intent(this@AccountLinkingConfirmActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()

                } else {
                    GeneralUtils(this@AccountLinkingConfirmActivity).showDialog("Failed")
                }
            }
        }

    }


}
